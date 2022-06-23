package aofc.transcoder.params;

import aofc.transcoder.Codec;
import org.jetbrains.annotations.NotNull;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.info.AudioInfo;

public class OpusTranscodeParams implements TranscodeParamsGenerator {
  private static final Codec CODEC = Codec.FLAC;
  private static final int DEFAULT_BIT_RATE = 256_000;
  private static final int DEFAULT_SAMPLING_RATE = 48_000;
  private static final int DEFAULT_QUALITY = 5;

  @Override
  public @NotNull EncodingAttributes getNewAttributesFrom(@NotNull AudioInfo info) {
    var res = new EncodingAttributes();
    var audio = new AudioAttributes();
    res.setAudioAttributes(audio);

    audio.setCodec(CODEC.getArg());
    audio.setBitRate(DEFAULT_BIT_RATE);
    audio.setSamplingRate(Math.min(info.getSamplingRate(), DEFAULT_SAMPLING_RATE));
    audio.setChannels(info.getChannels());
    audio.setQuality(DEFAULT_QUALITY);
    return res;
  }
}
