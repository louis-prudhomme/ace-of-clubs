package org.example.aofc.files;

import lombok.NonNull;
import org.example.aofc.files.exception.MusicFileException;

import java.util.Locale;
import java.util.Optional;

public class MusicFileFactory {
  public IMusicFile make(@NonNull String path) throws MusicFileException {
    return switch (getExtensionByStringHandling(path).orElseThrow().toLowerCase(Locale.ROOT)) {
      case "mp3" -> new Mp3MusicFile(path);
      case "flac" -> new FlacFile(path);
      default -> throw new RuntimeException(path);
    };
  }

  private static @NonNull Optional<String> getExtensionByStringHandling(@NonNull String filename) {
    return Optional.of(filename)
            .filter(f -> f.contains("."))
            .map(f -> f.substring(filename.lastIndexOf(".") + 1));
  }
}
