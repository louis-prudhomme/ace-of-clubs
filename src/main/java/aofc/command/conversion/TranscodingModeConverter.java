package aofc.command.conversion;

import aofc.transcoder.TranscodingModes;
import picocli.CommandLine;

import java.text.ParseException;

public class TranscodingModeConverter implements CommandLine.ITypeConverter<TranscodingModes> {
  @Override
  public TranscodingModes convert(String s) {
    try {
      return TranscodingModes.parseFrom(Integer.parseInt(s));
    } catch (ParseException e) {
      throw new CommandLine.TypeConversionException(
          String.format(
              "Unrecognized option « %s » for move mode ; must be of %s",
              s, TranscodingModes.concatenate(", ")));
    }
  }
}
