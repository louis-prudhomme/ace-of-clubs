package org.example.aofc;

import lombok.NonNull;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.formatter.exception.SpecificationParsingException;
import org.example.aofc.scrapper.Flagger;
import org.example.aofc.transponder.Transponder;
import org.example.aofc.utils.CheckPathMode;
import org.example.aofc.utils.FileUtils;
import org.example.aofc.utils.LoggerConfig;
import org.example.aofc.writer.MoveMode;
import org.example.aofc.writer.Mover;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Command(
    name = "aofc",
    mixinStandardHelpOptions = true,
    description = "Music file sorter.",
    version = "0.1")
public class AofC implements Callable<Integer> {
  @Parameters(index = "0", defaultValue = ".", description = "Path of the folder to index and sort")
  private String originPathArg;

  @Parameters(
      index = "1",
      defaultValue = "./Sorted/",
      description = "Path of the folder where to move the music files")
  private String destinationPathArg;

  @Option(
      names = {"-f", "--format"},
      description = "Format of the file names.",
      defaultValue = "[albart]/[date] – [album]/[track] – [title].[extension]")
  private String formatArg;

  @Option(
      names = {"-t", "--timeout"},
      description = "How much time do you want to wait for completion (in seconds).",
      defaultValue = "30")
  private Integer timeout;
  // todo add condition engine
  // todo add threading engine
  // todo add naming engine
  // todo add error engine

  @Override
  public Integer call() throws InterruptedException {
    var originPath = FileUtils.checkPath(this.originPathArg);
    var destinationPath = FileUtils.checkPath(this.destinationPathArg, CheckPathMode.OSEF);
    SpecificationFormatter specification;
    try {
      specification = new SpecificationFormatter(formatArg);
    } catch (SpecificationParsingException e) {
      throw new RuntimeException(e);
    }

    LoggerConfig.ConfigureLogger();

    var pool = ForkJoinPool.commonPool();
    var scrapper = new Flagger(pool, originPath, p -> true);
    var transponder = new Transponder(specification);
    var mover = new Mover(destinationPath, MoveMode.REPLACE_EXISTING);

    transponder.subscribe(mover);
    scrapper.subscribe(transponder);

    return !pool.awaitQuiescence(timeout, TimeUnit.SECONDS) ? 0 : 1;
  }

  public static void main(@NonNull String[] args) {
    System.exit(new CommandLine(new AofC()).execute(args));
  }
}
