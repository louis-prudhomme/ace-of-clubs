package aofc.transcoder.params;

import aofc.transcoder.Codec;
import reactor.util.annotation.NonNull;

public class TranscodeParamsGeneratorFactory {
  public @NonNull TranscodeParamsGenerator generateFor(@NonNull Codec codec) {
    return switch (codec) {
      case FLAC -> new FlacTranscodeParams();
      case OPUS -> new OpusTranscodeParams();
    };
  }
}
