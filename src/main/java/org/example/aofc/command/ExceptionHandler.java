package org.example.aofc.command;

import org.example.aofc.formatter.exception.SpecificationParsingException;
import picocli.CommandLine;

public class ExceptionHandler implements CommandLine.IExecutionExceptionHandler {

  @Override
  public int handleExecutionException(
      Exception e, CommandLine commandLine, CommandLine.ParseResult parseResult) {

    commandLine.getErr().println(commandLine.getColorScheme().errorText(e.getMessage()));
    if (e instanceof SpecificationParsingException) commandLine.usage(commandLine.getOut());

    return commandLine.getExitCodeExceptionMapper() != null
        ? commandLine.getExitCodeExceptionMapper().getExitCode(e)
        : commandLine.getCommandSpec().exitCodeOnExecutionException();
  }
}
