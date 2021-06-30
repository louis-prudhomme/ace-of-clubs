package aofc.formatter;

import aofc.formatter.exception.MalformedSpecificationException;
import aofc.formatter.exception.SpecificationParsingException;
import aofc.formatter.exception.UnevenSpecificationException;
import aofc.reader.MusicFile;
import aofc.reader.MusicTags;
import aofc.transponder.Sanitizer;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SpecificationFormatter {
  private static final char MARKER_LEFT = '[';
  private static final char MARKER_RIGHT = ']';
  private static final String EMPTY_TAG_PLACEHOLDER = "_";

  private final String rawSpec;
  private final List<Function<MusicFile, Optional<String>>> providers =
      new ArrayList<>(); // todo not optional, directly strings
  private final Sanitizer sanitizer = new Sanitizer();

  private final String specification;

  public SpecificationFormatter(@NonNull String rawSpec) throws SpecificationParsingException {
    this.rawSpec = rawSpec;

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
        else if (sanitizer.isAllowedInSpec(current))
          specAssembler.append(current); // otherwise if correct add
        else throw new MalformedSpecificationException(rawSpec);
      }
    }

    return specAssembler.toString();
  }

  public @NonNull String format(@NonNull MusicFile file) {
    try {
      return String.format(
          specification,
          providers.stream()
              .map(provider -> provider.apply(file))
              .map(s -> s.orElse(EMPTY_TAG_PLACEHOLDER))
              .map(sanitizer::sanitize)
              .toArray(Object[]::new));
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private void checkEvenSpecOrThrow() throws UnevenSpecificationException {
    long left = rawSpec.chars().filter(c -> c == MARKER_LEFT).count();
    long right = rawSpec.chars().filter(c -> c == MARKER_RIGHT).count();
    if (left != right) throw new UnevenSpecificationException(left, right);
  }
}