package org.example.aofc.reader;

import lombok.NonNull;
import org.example.aofc.reader.exception.MusicFileException;
import org.example.aofc.reader.exception.UnsupportedFormatException;
import org.example.aofc.utils.FileUtils;

import java.nio.file.Path;
import java.util.Locale;

public class MusicFileFactory {
  public @NonNull MusicFile make(@NonNull Path path) throws MusicFileException {
    return switch (FileUtils.getExtension(path).orElseThrow().toLowerCase(Locale.ROOT)) {
      case "mp3" -> new Mp3MusicFile(path);
      case "flac", "m4a" -> new FlacMusicFile(path);
      case "wav" -> throw new UnsupportedFormatException("wav");
      default -> throw new MusicFileException(path.toString());
    };
  }
}
