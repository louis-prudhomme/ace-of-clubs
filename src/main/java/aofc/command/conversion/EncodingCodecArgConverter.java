package aofc.command.conversion;

import aofc.transcoder.Codec;
import picocli.CommandLine;

import java.text.ParseException;

public class EncodingCodecArgConverter implements CommandLine.ITypeConverter<Codec> {
  @Override
  public Codec convert(String s) {
    try {
      return Codec.parseFrom(s);
    } catch (ParseException e) {
      throw new CommandLine.TypeConversionException(
          String.format(
              "Unrecognized option « %s » for move mode ; must be of %s",
              s, Codec.concatenate(", ")));
    }
  }
}
