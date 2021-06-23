package org.example.aofc.utils;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public class FileUtils {
  private static final List<String> MUSIC_EXTENSIONS = List.of("mp3", "flac");
  private static final String REPLACEMENT_CHAR = "_"; // todo add to commandline options
  private static final String[] FORBIDDEN_CHARS = {
    "/", "\\", "<", ">", "|", "\"", "*", "\n", "\t", ".", "[", "]", ",", ";", ":"
  };

  public static @NonNull Optional<String> getExtension(@NonNull String filename) {
    return Optional.of(filename)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.lastIndexOf(".") + 1))
        .map(String::toLowerCase);
  }

  public static boolean isMusicFile(@NonNull String path) {
    return getExtension(path).map(MUSIC_EXTENSIONS::contains).orElse(false);
  }

  public static @NonNull String sanitize(@NonNull String wouldBePathPiece) {
    String res = wouldBePathPiece;
    for (var forbidden : FORBIDDEN_CHARS)
      res = wouldBePathPiece.replace(forbidden, REPLACEMENT_CHAR);
    return res.trim();
  }
}
