package aofc.reader;

import aofc.formatter.exception.NoExtensionPresentException;
import aofc.reader.exception.MusicFileException;
import aofc.reader.exception.UnsupportedFormatException;
import aofc.utils.FileUtils;
import lombok.NonNull;

import java.nio.file.Path;
import java.util.Locale;

public class MusicFileFactory {
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
}
