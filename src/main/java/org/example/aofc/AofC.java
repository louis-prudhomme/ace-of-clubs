package org.example.aofc;

import lombok.NonNull;
import org.example.aofc.scrapper.Scrapper;
import org.example.aofc.utils.LoggerConfig;
import org.example.aofc.writer.Transponder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@Command(name = "aofc", mixinStandardHelpOptions = true, description = "Music file sorter.")
public class AofC implements Callable<Integer> {
  @Parameters(index = "0", defaultValue = ".", description = "Path of the folder to index and sort")
  private String pathArgument;

  @Option(
      names = {"-t", "--timeout"},
      description = "How much time do you want to wait for completion (in seconds).",
      defaultValue = "30")
  private Integer timeout = 30;
  // todo add condition engine
  // todo add threading engine
  // todo add naming engine
  // todo add destination path

  private Path checkPath(@NonNull String given) {
    try {
      var path = Path.of(given);
      if (!Files.isDirectory(path))
        throw new IllegalArgumentException(
            String.format("%s is not a directory.", path.toString()));
      return path;
    } catch (InvalidPathException e) {
      throw new IllegalArgumentException(String.format("%s is not a valid path", given));
    }
  }

  @Override
  public Integer call() throws InterruptedException {
    var targetPath = checkPath(pathArgument);
    LoggerConfig.ConfigureLogger();

    var pool = ForkJoinPool.commonPool();
    var scrapper = new Scrapper(pool, targetPath, s -> true);
    var transponder = new Transponder();

    scrapper.subscribe(transponder);
    return pool.awaitTermination(timeout, TimeUnit.SECONDS) ? 0 : 1;
  }

  public static void main(String[] args) {
    System.exit(new CommandLine(new AofC()).execute(args));
  }
}
