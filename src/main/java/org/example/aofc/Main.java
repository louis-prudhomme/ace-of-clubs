package org.example.aofc;

import org.example.aofc.scrapper.Scrapper;
import org.example.aofc.utils.LoggerConfig;
import org.example.aofc.writer.Transponder;

import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    LoggerConfig.ConfigureLogger();

    var pool = ForkJoinPool.commonPool();
    var scrapper = new Scrapper(pool, Path.of("D:/hella/Downloads"));
    var transponder = new Transponder();

    scrapper.subscribe(transponder);
    pool.awaitTermination(30, TimeUnit.SECONDS);
  }
}
