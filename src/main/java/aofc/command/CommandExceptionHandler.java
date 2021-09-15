package aofc.command;

import aofc.formatter.exception.SpecificationParsingException;
import picocli.CommandLine;

public class CommandExceptionHandler implements CommandLine.IExecutionExceptionHandler {

  @Override
  public int handleExecutionException(
      Exception e, CommandLine commandLine, CommandLine.ParseResult parseResult) {

    commandLine
        .getErr()
        .println(
            commandLine
                .getColorScheme()
                .errorText(e.getMessage() != null ? e.getMessage() : e.toString()));
    if (e instanceof SpecificationParsingException) commandLine.usage(commandLine.getOut());

    return commandLine.getExitCodeExceptionMapper() != null
        ? commandLine.getExitCodeExceptionMapper().getExitCode(e)
        : commandLine.getCommandSpec().exitCodeOnExecutionException();
  }
}
