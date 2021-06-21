package org.example.aofc.utils;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;

public class DateUtils {
  private static final String[] SEPARATORS = new String[] {"-", "/"};

  public static @NonNull Optional<String> getDate(@NonNull String date) {
    if (date.length() == 4) return Optional.of(date);
    return getYear(date);
  }

  private static @NonNull Optional<String> getYear(@NonNull String date) {
    return Arrays.stream(date.split(getSeparator(date)))
        .filter(piece -> piece.length() == 4)
        .findFirst();
  }

  private static @NonNull String getSeparator(@NonNull String date) {
    for (String sep : SEPARATORS) {
      if (date.contains(sep)) return sep;
    }
    throw new RuntimeException(date);
  }
}
