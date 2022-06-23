package aofc.transcoder.params;

import aofc.transcoder.Codec;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;

public class FlacTranscodeParams implements TranscodeParamsGenerator {
  private static final Codec CODEC = Codec.FLAC;
  private static final int DEFAULT_SAMPLING_RATE = 48_000;

  @Override
  public @NotNull EncodingAttributes getNewAttributesFrom(@NotNull AudioInfo info) {
    var res = new EncodingAttributes();
    var audio = new AudioAttributes();
    res.setAudioAttributes(audio);

    audio.setCodec(CODEC.getArg());
    audio.setSamplingRate(Math.min(info.getSamplingRate(), DEFAULT_SAMPLING_RATE));
    audio.setChannels(info.getChannels());

    return res;
  }
}
