package org.example.aofc.command;

import lombok.NonNull;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.scrapper.Flagger;
import org.example.aofc.transponder.Transponder;
import org.example.aofc.utils.CheckPathMode;
import org.example.aofc.utils.FileUtils;
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
    version = "0.1")
public class AofC implements Callable<Integer> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  static {
    java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
  }

  @Parameters(index = "0", defaultValue = ".", description = "Path of the folder to index and sort")
  private String originPathArg;

  @Parameters(
      index = "1",
      defaultValue = "./Sorted/",
      description = "Path of the folder where to move the music files")
  private String destinationPathArg;

  @Option(
      names = {"-f", "--format"},
      description =
          "Format of the file names, including any subfolders. Music file tags and data can be specified between [square brackets]. The default value is « [albart]/[date] – [album]/[track] – [title].[extension] »",
      defaultValue = "[albart]/[date] – [album]/[track] – [title].[extension]")
  private String specificationArg;

  @Option(
      names = {"-t", "--timeout"},
      description = "How much time do you want to wait for completion (in seconds).",
      defaultValue = "3")
  private Integer timeout;
  // todo add condition engine
  // todo add threading engine
  // todo add naming engine
  // todo add error engine

  @Override
  public Integer call() {
    var originPath = FileUtils.checkPath(this.originPathArg);
    var destinationPath = FileUtils.checkPath(this.destinationPathArg, CheckPathMode.OSEF);
    var specification = new SpecificationFormatter(specificationArg);

    logger.debug(String.format("Origin « %s »", originPath.toString()));
    logger.debug(String.format("Destination « %s »", destinationPath.toString()));
    logger.debug(String.format("Specification « %s »", specificationArg));
    logger.debug(String.format("Timeout %d seconds", timeout));

    // LoggerConfig.configureLogger();

    var pool = ForkJoinPool.commonPool();
    var scrapper = new Flagger(pool, originPath, p -> true);
    var transponder = new Transponder(specification, pool);
    var mover = new Mover(destinationPath, MoveMode.REPLACE_EXISTING);

    transponder.subscribe(mover);
    scrapper.subscribe(transponder);

    return pool.awaitQuiescence(timeout, TimeUnit.SECONDS) ? 0 : 1;
  }

  public static void main(@NonNull String[] args) {
    System.exit(
        new CommandLine(new AofC())
            .setExecutionExceptionHandler(new ExceptionHandler())
            .execute(args));
  }
}
