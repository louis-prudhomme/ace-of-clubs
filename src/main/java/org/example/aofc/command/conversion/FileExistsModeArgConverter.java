package org.example.aofc.command.conversion;

import org.example.aofc.writer.FileExistsMode;
import picocli.CommandLine;

import java.text.ParseException;

public class FileExistsModeArgConverter implements CommandLine.ITypeConverter<FileExistsMode> {
  @Override
  public FileExistsMode convert(String s) {
    try {
      return FileExistsMode.parseFrom(s);
    } catch (ParseException e) {
      throw new CommandLine.TypeConversionException(
          String.format(
              "Unrecognized option « %s » for move mode ; must be of %s",
              s, FileExistsMode.concatenate(", ")));
    }
  }
}
