package aofc.utils;

import lombok.NonNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class FileUtils {
  private static final List<String> MUSIC_EXTENSIONS = List.of("mp3", "flac");

  public static @NonNull Optional<String> getExtension(@NonNull Path filename) {
    return Optional.of(filename)
        .map(Path::toString)
        .filter(f -> f.contains("."))
        .map(f -> f.substring(filename.toString().lastIndexOf(".") + 1))
        .map(String::toLowerCase);
  }

  public static boolean isMusicFile(@NonNull Path path) {
    return getExtension(path).map(MUSIC_EXTENSIONS::contains).orElse(false);
  }

  public static boolean isFileAnyOf(@NonNull Path path, @NonNull List<String> extensions) {
    return getExtension(path).map(extensions::contains).orElse(false);
  }

  public static Path checkPath(@NonNull String given) {
    return checkPath(given, CheckPathMode.CHECK_IF_EXISTS);
  }

  public static Path checkPath(@NonNull String given, @NonNull CheckPathMode mode) {
    try {
      var path = Path.of(given);
      if (mode == CheckPathMode.CHECK_IF_EXISTS && !Files.isDirectory(path))
        throw new IllegalArgumentException(
            String.format("%s is not a directory.", path.toString()));
      else if (mode == CheckPathMode.CHECK_IF_CLEAR && Files.isDirectory(path))
        throw new IllegalArgumentException(String.format("%s already exists.", path.toString()));
      return path;
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("%s is not a valid path", given));
    }
  }

  // todo probably broken
  public static String getShortName(@NonNull Path path, int nb) {
    if (path.getNameCount() > nb) throw new IllegalArgumentException();
    return path.subpath(path.getNameCount() - nb, path.getNameCount()).toString();
  }
}
