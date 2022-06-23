package aofc.transcoder;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum Codec {
  FLAC("flac"),
  OPUS("libopus");

  @Getter private final String arg;

  public static Codec parseFrom(@NonNull String s) throws ParseException {
    return Arrays.stream(values())
        .filter(modes -> modes.arg.equals(s.toLowerCase(Locale.ROOT)))
        .findFirst()
        .orElseThrow(() -> new ParseException(s, 0));
  }

  public static @NonNull String concatenate(@NonNull String separator) {
    return Arrays.stream(values())
        .map(Codec::getArg)
        .reduce((moveMode, moveMode2) -> String.join(separator, moveMode, moveMode2))
        .orElseThrow();
  }

  public static class Enumeration extends ArrayList<String> {
    public Enumeration() {
      super(Arrays.stream(Codec.values()).map(Codec::getArg).collect(Collectors.toList()));
    }
  }
}
