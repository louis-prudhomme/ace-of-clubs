package aofc.formatter;

import aofc.formatter.exception.MalformedSpecificationException;
import aofc.formatter.exception.SpecificationParsingException;
import aofc.formatter.exception.UnevenSpecificationException;
import aofc.formatter.provider.TagProvider;
import aofc.formatter.provider.TagProviderMapper;
import aofc.formatter.provider.exception.TagProviderException;
import aofc.reader.MusicFile;
import aofc.reader.MusicTags;
import aofc.transponder.PathSanitizer;
import lombok.NonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// todo find parsing library
public class SpecificationFormatter {
  private static final char MARKER_LEFT = '[';
  private static final char MARKER_RIGHT = ']';
  private static final String EMPTY_TAG_PLACEHOLDER = "_";

  private final String rawSpec;
  private final List<TagProvider> providers =
      new ArrayList<>(); // todo not optional, directly strings
  private final PathSanitizer pathSanitizer;

  private final String specification;

  public SpecificationFormatter(@NonNull String rawSpec, @NonNull String replacementCharacter)
      throws SpecificationParsingException {
    this.rawSpec = rawSpec;
    this.pathSanitizer = new PathSanitizer(replacementCharacter);

    checkEvenSpecOrThrow();
    this.specification = tryParseBaseTextOrThrow();
  }

  private @NonNull String tryParseBaseTextOrThrow() throws SpecificationParsingException {
    var specAssembler = new StringBuilder();
    var keyAssembler = new StringBuilder();

    boolean w8 = false;

    for (int i = 0; i < rawSpec.length(); i++) {
      char current = rawSpec.charAt(i);

      if (w8) { // if pending key
        if (current == MARKER_LEFT)
          throw new MalformedSpecificationException(rawSpec); // if marker left, malformed
        else if (current == MARKER_RIGHT) { // if marker right end key & try parse
          w8 = false;
          providers.add(
              TagProviderMapper.getProviderFor(MusicTags.parseFrom(keyAssembler.toString())));
          specAssembler.append("%s");
        } else keyAssembler.append(current); // otherwise, compose key

      } else { // if no pending key
        if (current == MARKER_LEFT) {
          keyAssembler = new StringBuilder();
          w8 = true;
        } else if (current == MARKER_RIGHT)
          throw new MalformedSpecificationException(rawSpec); // right is malformed
        else if (pathSanitizer.isAllowedInSpec(current))
          specAssembler.append(current); // otherwise if correct add
        else throw new MalformedSpecificationException(rawSpec);
      }
    }

    return specAssembler.toString();
  }

  public @NonNull Path format(@NonNull Path destination, @NonNull MusicFile file)
      throws TagProviderException {
    return pathSanitizer.trimToLength(destination.resolve(Path.of(assemblePath(file))));
  }

  private @NonNull String assemblePath(@NonNull MusicFile file) throws TagProviderException {
    var tags = new ArrayList<Optional<String>>();
    for (var provider : providers) tags.add(provider.apply(file));

    return String.format(
        specification,
        tags.stream()
            .map(s -> s.orElse(EMPTY_TAG_PLACEHOLDER))
            .map(pathSanitizer::sanitize)
            .toArray(Object[]::new));
  }

  private void checkEvenSpecOrThrow() throws UnevenSpecificationException {
    long left = rawSpec.chars().filter(c -> c == MARKER_LEFT).count();
    long right = rawSpec.chars().filter(c -> c == MARKER_RIGHT).count();
    if (left != right) throw new UnevenSpecificationException(left, right);
  }
}
