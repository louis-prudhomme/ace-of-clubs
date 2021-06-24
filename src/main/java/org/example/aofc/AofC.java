package org.example.aofc;

import lombok.NonNull;
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

@Command(name = "aofc", mixinStandardHelpOptions = true, description = "Music file sorter.")
public class AofC implements Callable<Integer> {
  @Parameters(index = "0", defaultValue = ".", description = "Path of the folder to index and sort")
  private String originPathArg;

  @Parameters(
      index = "1",
      defaultValue = "./Sorted/",
      description = "Path of the folder where to move the music files")
  private String destinationPathArg;

  @Option(
      names = {"-t", "--timeout"},
      description = "How much time do you want to wait for completion (in seconds).",
      defaultValue = "30")
  private Integer timeout = 30;
  // todo add condition engine
  // todo add threading engine
  // todo add naming engine

  @Override
  public Integer call() throws InterruptedException {
    var originPath = FileUtils.checkPath(this.originPathArg);
    var destinationPath = FileUtils.checkPath(this.destinationPathArg, CheckPathMode.OSEF);

    LoggerConfig.ConfigureLogger();

    var pool = ForkJoinPool.commonPool();
    var scrapper = new Flagger(pool, originPath, p -> true);
    var transponder = new Transponder();
    var mover = new Mover(destinationPath, MoveMode.REPLACE_EXISTING);

    transponder.subscribe(mover);
    scrapper.subscribe(transponder);

    return pool.awaitTermination(timeout, TimeUnit.SECONDS) ? 0 : 1;
  }

  public static void main(@NonNull String[] args) {
    System.exit(new CommandLine(new AofC()).execute(args));
  }
}
