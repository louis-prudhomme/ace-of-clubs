package aofc.transcoder;

import aofc.formatter.MusicFileVorbisTags;
import aofc.reader.MusicFile;
import aofc.reader.MusicFileFactory;
import aofc.reader.MusicTags;
import aofc.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.gagravarr.flac.FlacNativeFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;
import org.gagravarr.flac.FlacFile;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
public class Transcoder implements Function<Path, Flux<Path>> {
  private final Logger logger = LoggerFactory.getLogger("aofc");
  private static final int OPUS_DEFAULT_BIT_RATE = 192_000;
  private static final Set<String> FORMATS_TO_TRANSCODE = Set.of("wav", "mp3");

  private final MusicFileFactory factory = new MusicFileFactory();

  private final boolean ignoreGoodEncodings;
  private final EncodingCodecs codec;

  private static final int BATCH_QUANTITY = 10;
  private static final int PATH_LIMITATION = 250;
  private static final AtomicLong handled = new AtomicLong(0);

  @Override
  public @NonNull Flux<Path> apply(@NonNull Path transcodat) {
    // if file already has a good format, leave it be
    var extension = FileUtils.getExtension(transcodat);

    if (extension.isEmpty()
        || (!FORMATS_TO_TRANSCODE.contains(extension.get()) && !ignoreGoodEncodings)) {
      logger.debug(String.format("No need to transcode %s", transcodat));
      return Flux.just(transcodat);
    }

    if (!MusicFileFactory.isMusicFile(transcodat)) {
      logger.warn("« {} » was not a music file.", transcodat.getFileName().toString());
      return Flux.empty();
    }

    Path transcodedPath = null;
    try {
      var multimedia = new MultimediaObject(transcodat.toFile());
      var attributes = getNewAttributesFrom(multimedia.getInfo().getAudio());

      if (codec == EncodingCodecs.OPUS) {
        var flac = new FlacNativeFile(transcodat.toFile());
        var tags = flac.getTags();
      }

      transcodedPath = getNewPath(transcodat, extension.get());

      var transcodedPathSize = transcodedPath.toString().length();
      if (transcodedPathSize > PATH_LIMITATION) { // fixme there must be a way to circumvent this…
        logger.error(
            "Could not transcode « {} » because the new path is too long ({} = {} characters).",
            transcodat,
            transcodedPath,
            transcodedPathSize);
        return Flux.empty();
      }

      new Encoder().encode(multimedia, transcodedPath.toFile(), attributes);

      logger.debug(String.format("Transcoded « %s »", transcodedPath));
      if (handled.incrementAndGet() % BATCH_QUANTITY == 0)
        logger.info("Transcoded {}-th file.", handled.get());

      return Flux.just(transcodedPath);
    } catch (IllegalStateException e) {
      logger.warn(
          "Transcoding of « {} » interrupted by shutdown, attempting to delete the file.",
          transcodat);
      if (transcodedPath != null) {
        var shaggedFile = transcodedPath.toFile();
        if (shaggedFile.exists()) shaggedFile.deleteOnExit();
      }
      return Flux.empty();
    } catch (EncoderException e) {
      logger.warn("Transcoding of « {} » failed, attempting to delete the file.", transcodat);
      if (transcodedPath != null) {
        var shaggedFile = transcodedPath.toFile();
        if (shaggedFile.exists()) shaggedFile.deleteOnExit();
      }
      return Flux.error(e);
    } catch (Exception e) {
      logger.error(
          "Could not transcode « {} » : {}",
          transcodat,
          e.getMessage() != null
              ? e.getMessage()
              : e.getCause() != null ? e.getCause() : e.toString());
      return Flux.empty();
    }
  }

  // fixme probably broken because of windows path limitation
  private @NonNull Path getNewPath(@NonNull Path old, @NonNull String oldExt) {
    var oldFileName = old.getFileName().toString();
    var oldLength = oldFileName.length();

    var fn =
        oldFileName.substring(0, oldLength - oldExt.length())
            + MusicFileExtension.FromEncodingCodec(codec).getArg();
    return old.getParent().resolve(fn);
  }

  private @NonNull EncodingAttributes getNewAttributesFrom(@NonNull AudioInfo object) {
    var bitrate = codec == EncodingCodecs.OPUS ? OPUS_DEFAULT_BIT_RATE : object.getBitRate();

    var res = new EncodingAttributes();
    var audio = new AudioAttributes();

    res.setAudioAttributes(audio);
    audio.setCodec(codec.getArg());
    audio.setBitRate(bitrate);
    if (codec == EncodingCodecs.FLAC) audio.setSamplingRate(object.getSamplingRate());
    audio.setChannels(object.getChannels());
    return res;
  }

  private @NonNull Map<String, String> getNewMultimediaAttributes(
      @NonNull MusicFile oldMusicFile, @NonNull MultimediaObject multimediaObject)
      throws EncoderException {
    var oldMetaData = multimediaObject.getInfo().getMetadata();
    var tags =
        MusicTags.getUbiquitousTags().stream()
            .map(tag -> Pair.of(MusicFileVorbisTags.convert(tag), oldMusicFile.getTag(tag)))
            .filter(tuple -> tuple.getKey().isPresent())
            .collect(
                Collectors.toMap(
                    tuple -> tuple.getLeft().get().getKey(), tuple -> tuple.getRight().orElse("")));
    tags.forEach((key, value) -> oldMetaData.merge(key, value, (v1, v2) -> v2));
    return tags;
  }
}
