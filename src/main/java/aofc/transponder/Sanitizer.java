package aofc.transponder;

import aofc.utils.FileUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class Sanitizer {
  private static final String[] FORBIDDEN_CHARS_SPEC = {
    "<", ">", "|", "\"", "*", "\n", "\t", "[", "]", "?", ",", ";", ":"
  };
  private static final String[] EXTRA_FORBIDDEN_CHARS = {"/", "\\", "."};
  public static final String[] FORBIDDEN_CHARS =
      Stream.concat(Arrays.stream(FORBIDDEN_CHARS_SPEC), Arrays.stream(EXTRA_FORBIDDEN_CHARS))
          .toArray(String[]::new);
  private static final int MAX_PATH_LENGTH = 260;

  private final String replacementCharacter;

  public @NonNull String sanitize(@NonNull String wouldBePathPiece) {
    String res = wouldBePathPiece;
    for (var forbidden : FORBIDDEN_CHARS) res = res.replace(forbidden, replacementCharacter);

    return res.trim();
  }

  // todo supplement this with customization of pieces length
  public @NonNull Path trimToLength(@NonNull Path toCheck) {
    if (toCheck.toString().length() < MAX_PATH_LENGTH) return toCheck;

    var pathLength = toCheck.toString().length();
    var filename = toCheck.getFileName();
    var filenameLength = filename.toString().length();
    var extension = FileUtils.getExtension(filename).map(ext -> "." + ext).orElse("");
    var extensionLength = extension.length();
    var diff = pathLength - MAX_PATH_LENGTH;

    // todo
    if (diff >= filenameLength - extensionLength)
      throw new RuntimeException(String.format("Unsolvable max path length for « %s »", toCheck));

    var trimmed = filename.toString().substring(0, filenameLength - diff - extensionLength);
    return toCheck.getParent().resolve(trimmed.trim() + extension);
  }

  public boolean isAllowedInSpec(char c) {
    return Arrays.stream(FORBIDDEN_CHARS_SPEC).noneMatch(s -> s.equals(String.valueOf(c)));
  }
}
