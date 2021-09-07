package aofc.transcoder;

import aofc.transponder.EncodingCodecs;
import aofc.utils.AbstractQueuedSubscription;
import aofc.utils.FileUtils;
import lombok.NonNull;
import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;

import java.nio.file.Path;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

public class TranscoderSubscription extends AbstractQueuedSubscription<Path, Path> {
  private static final Set<String> FORMATS_TO_ENCODE = Set.of("wav", "mp3");

  private final EncodingCodecs codec;
  private final Encoder encoder = new Encoder();

  public TranscoderSubscription(
      @NonNull ExecutorService executor,
      @NonNull Queue<Path> queue,
      @NonNull Flow.Subscriber<? super Path> subscriber,
      @NonNull EncodingCodecs codec) {
    super(executor, queue, subscriber);
    this.codec = codec;
  }

  @Override
  protected void consume(@NonNull Path transcodat) {
    // if file already has a good format, leave it be
    var extension = FileUtils.getExtension(transcodat).orElseThrow();

    if (!FORMATS_TO_ENCODE.contains(extension)) {
      logger.debug(String.format("No need to transcode %s", transcodat));
      executor.execute(() -> subscriber.onNext(transcodat));
      return;
    }

    try {
      var multimedia = new MultimediaObject(transcodat.toFile());
      var attributes = getNewAttributesFrom(multimedia.getInfo().getAudio());
      var transcodedPath = getNewPath(transcodat, extension);
      encoder.encode(multimedia, transcodedPath.toFile(), attributes);
      logger.debug(String.format("Transcoded %s", transcodedPath));

      executor.execute(() -> subscriber.onNext(transcodedPath));
    } catch (Exception e) {
      logger.error(e.toString());
      e.printStackTrace();
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
