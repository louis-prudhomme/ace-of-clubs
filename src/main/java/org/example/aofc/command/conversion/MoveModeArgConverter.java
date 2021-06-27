package org.example.aofc.command.conversion;

import org.example.aofc.writer.MoveMode;
import picocli.CommandLine;

import java.text.ParseException;

public class MoveModeArgConverter implements CommandLine.ITypeConverter<MoveMode> {
  @Override
  public MoveMode convert(String s) {
    try {
      return MoveMode.parseFrom(s);
    } catch (ParseException e) {
      throw new CommandLine.TypeConversionException(
          String.format(
              "Unrecognized option « %s » for move mode ; must be of %s",
              s, MoveMode.concatenate(", ")));
    }
  }
}
