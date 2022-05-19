package aofc.reader;

import aofc.formatter.exception.NoExtensionPresentException;
import aofc.reader.exception.MusicFileException;
import aofc.reader.exception.UnsupportedFormatException;
import aofc.utils.FileUtils;
import lombok.NonNull;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

public class MusicFileFactory {
  public static final Set<String> handledFormats = Set.of("mp3", "flac", "m4a", "ogg", "wav");

  public @NonNull MusicFile make(@NonNull Path path) throws MusicFileException {
    var extension = FileUtils.getExtension(path);
    if (extension.isEmpty()) throw new NoExtensionPresentException(path.toString());
    
    return switch (extension.get().toLowerCase(Locale.ROOT)) {
      case "mp3" -> new Mp3MusicFile(path);
      case "flac", "m4a", "ogg" -> new FlacMusicFile(path);
      case "wav" -> throw new UnsupportedFormatException("wav");
      default -> throw new MusicFileException(path.toString());
    };
  }

  public static boolean isMusicFile(@NonNull Path path) {
    return handledFormats.stream().anyMatch(ext -> path.getFileName().toString().endsWith(ext));
  }
}
