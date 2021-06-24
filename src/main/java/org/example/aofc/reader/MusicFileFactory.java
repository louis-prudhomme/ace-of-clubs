package org.example.aofc.reader;

import lombok.NonNull;
import org.example.aofc.reader.exception.MusicFileException;
import org.example.aofc.utils.FileUtils;

import java.nio.file.Path;
import java.util.Locale;

public class MusicFileFactory {
  public IMusicFile make(@NonNull Path path) throws MusicFileException {
    return switch (FileUtils.getExtension(path).orElseThrow().toLowerCase(Locale.ROOT)) {
      case "mp3" -> new Mp3MusicFile(path);
      case "flac" -> new FlacMusicFile(path);
      default -> throw new RuntimeException(path.toString());
    };
  }
}
