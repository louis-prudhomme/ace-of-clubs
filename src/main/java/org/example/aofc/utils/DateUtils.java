package org.example.aofc.utils;

import lombok.NonNull;

import java.util.Arrays;

public class DateUtils {
  private static final String[] SEPARATORS = new String[] {"-", "/"};

  public static @NonNull String getDate(@NonNull String date) {
    if (date.length() == 4) return date;
    return getYear(date);
  }

  private static @NonNull String getYear(@NonNull String date) {
    return Arrays.stream(date.split(getSeparator(date)))
        .filter(piece -> piece.length() == 4)
        .findFirst()
        .orElseThrow();
  }

  private static @NonNull String getSeparator(@NonNull String date) {
    for (String sep : SEPARATORS) {
      if (date.contains(sep)) return sep;
    }
    throw new RuntimeException(date);
  }
}
