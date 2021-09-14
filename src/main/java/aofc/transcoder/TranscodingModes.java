package aofc.transcoder;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum TranscodingModes {
  NO_TRANSCODING(0),
  MP3_AND_WAV(1),
  MP3_ONLY(10),
  WAV_ONLY(11);

  @Getter private final int arg;

  public static TranscodingModes parseFrom(@NonNull Integer raw) throws ParseException {
    return switch (raw) {
      case 0 -> NO_TRANSCODING;
      case 1 -> MP3_ONLY;
      case 10 -> WAV_ONLY;
      case 11 -> MP3_AND_WAV;
      default -> throw new ParseException(String.format("%s cannot be parsed to a valid transcoding mode", raw), 0);
    };
  }

  public static @NonNull String concatenate(@NonNull String separator) {
    return Arrays.stream(values())
        .map(tm -> String.format("%d (%s)", tm.getArg(), tm))
        .reduce((moveMode, moveMode2) -> String.join(separator, moveMode, moveMode2))
        .orElseThrow();
  }

  public static class Enumeration extends ArrayList<String> {
    public Enumeration() {
      super(Arrays.stream(TranscodingModes.values()).map(tm -> String.format("%d (%s)", tm.getArg(), tm)).collect(Collectors.toList()));
    }
  }
}
