package org.example.aofc.utils;

import lombok.NonNull;

import java.util.Optional;

public class DateUtils {
  private static final String[] SEPARATORS = new String[] {"-", "/"};

  public static @NonNull Optional<String> getProperDate(@NonNull String date) {
    if (date.length() == 4) return Optional.of(date);
    return getYear(date);
  }

  private static @NonNull Optional<String> getYear(@NonNull String date) {
    var datePieces = date.split(getSeparator(date));

    return datePieces.length > 1 && datePieces[datePieces.length - 1].length() == 4
        ? Optional.of(datePieces[datePieces.length - 1])
        : datePieces[datePieces.length - 1].length() == 2
            ? Optional.of(String.format("20%s", datePieces[datePieces.length - 1]))
            : Optional.empty();
  }

  private static @NonNull String getSeparator(@NonNull String date) {
    for (var sep : SEPARATORS) {
      if (date.contains(sep)) return sep;
    }

    throw new RuntimeException(date);
  }
}
