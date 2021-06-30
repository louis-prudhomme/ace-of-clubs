package aofc.writer;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public enum FileExistsMode {
  REPLACE_EXISTING("replace"),
  SKIP_IF_EXIST("skip");

  @Getter private final String arg;

  public static FileExistsMode parseFrom(@NonNull String s) throws ParseException {
    return Arrays.stream(values())
        .filter(modes -> modes.arg.equals(s.toLowerCase(Locale.ROOT)))
        .findFirst()
        .orElseThrow(() -> new ParseException(s, 0));
  }

  public static @NonNull String concatenate(@NonNull String separator) {
    return Arrays.stream(values())
        .map(FileExistsMode::getArg)
        .reduce((moveMode, moveMode2) -> String.join(separator, moveMode, moveMode2))
        .orElseThrow();
  }

  public static class Enumeration extends ArrayList<String> {
    public Enumeration() {
      super(
          Arrays.stream(FileExistsMode.values())
              .map(FileExistsMode::getArg)
              .collect(Collectors.toList()));
    }
  }
}
