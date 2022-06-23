package aofc.transcoder.params;

import reactor.util.annotation.NonNull;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;

public interface TranscodeParamsGenerator {
  @NonNull
  EncodingAttributes getNewAttributesFrom(@NonNull AudioInfo info);
}
