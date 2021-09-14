package aofc.transcoder;

import aofc.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
public class Transcoder implements Function<Path, Flux<Path>> {
  private final Logger logger = LoggerFactory.getLogger("aofc");
  private static final Set<String> FORMATS_TO_ENCODE = Set.of("wav", "mp3");

  private final Encoder encoder = new Encoder();
  private final EncodingCodecs codec;

  @Override
  public @NonNull Flux<Path> apply(@NonNull Path transcodat) {
    // if file already has a good format, leave it be
    var extension = FileUtils.getExtension(transcodat).orElseThrow();

    if (!FORMATS_TO_ENCODE.contains(extension)) {
      logger.debug(String.format("No need to transcode %s", transcodat));
      return Flux.just(transcodat);
    }

    try {
      var multimedia = new MultimediaObject(transcodat.toFile());
      var attributes = getNewAttributesFrom(multimedia.getInfo().getAudio());
      var transcodedPath = getNewPath(transcodat, extension);
      encoder.encode(multimedia, transcodedPath.toFile(), attributes);
      logger.debug(String.format("Transcoded « %s »", transcodedPath));

      return Flux.just(transcodedPath);
    } catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
      throw new NotImplementedException(); // fixme
    }
  }

  private @NonNull Path getNewPath(@NonNull Path old, @NonNull String oldExt) {
    var oldFileName = old.getFileName().toString();
    var oldLength = oldFileName.length();

    var fn = oldFileName.substring(0, oldLength - oldExt.length()) + codec.getArg();
    return old.getParent().resolve(fn);
  }

  private @NonNull EncodingAttributes getNewAttributesFrom(@NonNull AudioInfo object) {
    var res = new EncodingAttributes();
    var audio = new AudioAttributes();
    res.setAudioAttributes(audio);
    audio.setCodec(codec.getArg());
    audio.setBitRate(object.getBitRate());
    audio.setSamplingRate(object.getSamplingRate());
    audio.setChannels(object.getChannels());
    return res;
  }
}
