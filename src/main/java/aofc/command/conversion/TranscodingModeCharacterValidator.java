package aofc.command.conversion;

import picocli.CommandLine;

public class TranscodingModeCharacterValidator implements CommandLine.ITypeConverter<Integer> {
  @Override
  public Integer convert(String potential) throws Exception {
    try {
      int mode = Integer.parseInt(potential);
      if (mode >= 0 && mode <= 2) return mode;

      throw new CommandLine.TypeConversionException(
          String.format("« %s » is not a valid transcoding mode.", potential));
    } catch (NumberFormatException e) {
      throw new CommandLine.TypeConversionException(
          String.format("« %s » is not a valid transcoding mode.", potential));
    }
  }
}
