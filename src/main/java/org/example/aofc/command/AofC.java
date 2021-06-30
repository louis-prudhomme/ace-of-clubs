package org.example.aofc.command;

import lombok.NonNull;
import org.example.aofc.command.conversion.FileExistsModeArgConverter;
import org.example.aofc.command.conversion.MoveModeArgConverter;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.scrapper.FlaggerPublisher;
import org.example.aofc.transponder.TransponderProcessor;
import org.example.aofc.utils.CheckPathMode;
import org.example.aofc.utils.FileUtils;
import org.example.aofc.writer.FileExistsMode;
import org.example.aofc.writer.MoveMode;
import org.example.aofc.writer.Mover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Command(
    name = "aofc",
    mixinStandardHelpOptions = true,
    description = "Music file sorter.",
    version = "0.2",
    exitCodeList = {
      "0\t:\tSuccessful program execution.",
      "2\t:\tArg parsing error.",
      "1000\t:\tProgram timed out."
    })
public class AofC implements Callable<Integer> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  static {
    java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
  }

  @Parameters(
      index = "0",
      defaultValue = ".",
      description = "Path of the folder to index and sort.")
  private String originPathArg;

  @Parameters(
      index = "1",
      defaultValue = "./Sorted/",
      description = "Path of the folder where to move the music files.")
  private String destinationPathArg;

  @Option(
      names = {"-f", "--format"},
      description =
          "Format of the file names, including any subfolder. Music file tags and data can be specified between [square brackets]. The default value is « ${DEFAULT-VALUE} »",
      defaultValue = "[albart]/[date] – [album]/[disc]-[track] – [title].[extension]")
  private String specificationArg;

  @Option(
      names = {"-t", "--timeout"},
      description =
          "How much time do you want to wait for completion (in seconds). If zero, no timeout will be expected (the program will complete when all files are sorted). Default: ${DEFAULT-VALUE}",
      defaultValue = "0")
  private Integer timeout;

  @Option(
      names = {"-fem", "--file-exist-mode"},
      description =
          "What should the program do when a music file already exists. Must be one of « ${COMPLETION-CANDIDATES} ». Default is « ${DEFAULT-VALUE} ».",
      converter = FileExistsModeArgConverter.class,
      completionCandidates = FileExistsMode.Enumeration.class,
      defaultValue = "replace")
  private FileExistsMode fileExistsMode;

  @Option(
      names = {"-mm", "--move-mode"},
      description =
          "Whether the program should copy or move the files. Must be one of « ${COMPLETION-CANDIDATES} ». Default is « ${DEFAULT-VALUE} ».",
      converter = MoveModeArgConverter.class,
      completionCandidates = MoveMode.Enumeration.class,
      defaultValue = "move")
  private MoveMode moveMode;

  // todo fix logger formatting

  @Override
  public Integer call() {
    var originPath = FileUtils.checkPath(this.originPathArg);
    var destinationPath = FileUtils.checkPath(this.destinationPathArg, CheckPathMode.OSEF);
    var specification = new SpecificationFormatter(specificationArg);

    logger.debug(String.format("Origin « %s »", originPath.toString()));
    logger.debug(String.format("Destination « %s »", destinationPath.toString()));
    logger.debug(String.format("Specification « %s »", specificationArg));
    logger.debug(String.format("Timeout %d seconds", timeout));
    logger.debug(String.format("FileExistsMode « %s »", fileExistsMode.toString()));
    logger.debug(String.format("MoveMode « %s »", moveMode.toString()));

    var pool = ForkJoinPool.commonPool();
    var p1 = new ForkJoinPool();
    var p2 = new ForkJoinPool();

    var scrapper = new FlaggerPublisher(p1, originPath);
    var transponder = new TransponderProcessor(p2, specification);
    var mover = new Mover(destinationPath, fileExistsMode, moveMode);

    scrapper.subscribe(transponder);
    transponder.subscribe(mover);

    if (timeout <= 0) timeout = Integer.MAX_VALUE;

    return p1.awaitQuiescence(timeout, TimeUnit.SECONDS)
            && p2.awaitQuiescence(timeout, TimeUnit.SECONDS)
        ? 0
        : 1000;
  }

  public static void main(@NonNull String[] args) {
    System.exit(
        new CommandLine(new AofC())
            .setExecutionExceptionHandler(new ExceptionHandler())
            .execute(args));
  }
}
