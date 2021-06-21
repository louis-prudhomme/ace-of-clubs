package org.example.aofc.utils;

import lombok.NonNull;

import java.util.Optional;

public class FileUtils {
  public static @NonNull Optional<String> getExtension(@NonNull String filename) {
    return Optional.of(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }
}
