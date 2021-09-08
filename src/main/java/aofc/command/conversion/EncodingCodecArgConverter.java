package aofc.command.conversion;

import aofc.transcoder.EncodingCodecs;
import picocli.CommandLine;

import java.text.ParseException;

public class EncodingCodecArgConverter implements CommandLine.ITypeConverter<EncodingCodecs> {
  @Override
  public EncodingCodecs convert(String s) {
    try {
      return EncodingCodecs.parseFrom(s);
    } catch (ParseException e) {
      throw new CommandLine.TypeConversionException(
          String.format(
              "Unrecognized option « %s » for move mode ; must be of %s",
              s, EncodingCodecs.concatenate(", ")));
    }
  }
}
