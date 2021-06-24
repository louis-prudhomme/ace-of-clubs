package org.example.aofc.scrapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.aofc.utils.SyncingQueue;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

@RequiredArgsConstructor
public class Scrapper implements Runnable {
  private static final int PRODUCING_RATE = 100;

  @Getter private final SyncingQueue<Path> queue = new SyncingQueue<>();
  private final Path origin;
  private final Function<Path, Boolean> filterCriteria;

  @Getter private boolean completed = false;

  @Override
  public void run() {
    try (var files = Files.walk(origin, FileVisitOption.FOLLOW_LINKS)) {
      files
          .filter(Files::isRegularFile)
          .filter(filterCriteria::apply)
          .forEach(
              item -> {
                try {
                  produce(item);
                } catch (InterruptedException e) {
                  files.close();
                }
              });
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      completed = true;
    }
  }

  @SneakyThrows
  private void produce(@NonNull Path item) throws InterruptedException {
    synchronized (queue) {
      while (queue.size() > PRODUCING_RATE) queue.wait();
      queue.push(item);
      queue.notify();
    }
  }
}
