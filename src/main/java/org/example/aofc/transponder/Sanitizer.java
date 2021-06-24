package org.example.aofc.transponder;

import lombok.NonNull;
import org.example.aofc.utils.FileUtils;

import java.util.Arrays;
import java.util.Optional;

public class Sanitizer {
  private final Optional<String>[] args;

  @SafeVarargs // todo chek fuk
  public Sanitizer(Optional<String>... args) {
    this.args = args;
  }

  public @NonNull String[] santizeBulk() {
    return Arrays.stream(args)
        .map(Optional::orElseThrow)
        .map(FileUtils::sanitize)
        .toArray(String[]::new);
  }

  public int getNbArgs() {
    return args.length;
  }
}
