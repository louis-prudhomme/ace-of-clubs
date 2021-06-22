package org.example.aofc.utils;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public class FileUtils {
  public static final List<String> MUSIC_EXTENSIONS = List.of("mp3", "flac");

  public static @NonNull Optional<String> getExtension(@NonNull String filename) {
    return Optional.of(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1))
        .map(String::toLowerCase);
  }

  public static boolean isMusicFile(@NonNull String path) {
    return getExtension(path).map(MUSIC_EXTENSIONS::contains).orElse(false);
  }
}
