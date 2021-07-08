package aofc.command.conversion;

import aofc.transponder.Sanitizer;
import picocli.CommandLine;

import java.util.Arrays;

public class ReplacerCharacterValidator implements CommandLine.ITypeConverter<String> {
  @Override
  public String convert(String potentialReplacer) throws Exception {
    if (Arrays.stream(Sanitizer.FORBIDDEN_CHARS).anyMatch(potentialReplacer::contains))
      throw new CommandLine.TypeConversionException(
          String.format("« %s » is not allowed as a replacement character.", potentialReplacer));
    return potentialReplacer;
  }
}
