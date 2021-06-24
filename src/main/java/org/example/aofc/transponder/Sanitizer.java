package org.example.aofc.transponder;

import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;

public class Sanitizer {
  @Getter private static final String REPLACEMENT_CHAR = "_"; // todo add to commandline options
  private static final String[] FORBIDDEN_CHARS = {
    "/", "\\", "<", ">", "|", "\"", "*", "\n", "\t", ".", "[", "]", ",", ";", ":"
  };
  private static final String[] FORBIDDEN_CHARS_SPEC = {
    "\\", "<", ">", "|", "\"", "*", "\n", "\t", "[", "]", ",", ";", ":"
  };
  private final Optional<String>[] args;

  @SafeVarargs // todo chek fuk
  public Sanitizer(@NonNull Optional<String>... args) {
    this.args = args;
  }

  public @NonNull String sanitize(@NonNull String wouldBePathPiece) {
    String res = wouldBePathPiece;
    for (var forbidden : FORBIDDEN_CHARS)
      res = wouldBePathPiece.replace(forbidden, REPLACEMENT_CHAR);
    return res.trim();
  }

  public @NonNull String[] santizeBulk() {
    return Arrays.stream(args)
        .map(Optional::orElseThrow)
        .map(this::sanitize)
        .toArray(String[]::new);
  }

  public boolean isAllowedInSpec(char c) {
    return Arrays.stream(FORBIDDEN_CHARS_SPEC).noneMatch(s -> s.equals(String.valueOf(c)));
  }

  public int getNbArgs() {
    return args.length;
  }
}
