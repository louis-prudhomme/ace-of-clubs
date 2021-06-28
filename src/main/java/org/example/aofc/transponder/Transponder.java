package org.example.aofc.transponder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.reader.MusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.reader.exception.MusicFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;

@RequiredArgsConstructor
public class Transponder implements Flow.Subscription {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MusicFileFactory factory = new MusicFileFactory();
  private final List<Future<?>> futures = new ArrayList<>();

  private final SpecificationFormatter formatter;
  private final ExecutorService executor;
  private final Queue<Path> queue;
  private final Flow.Subscriber<? super Pair<Path, Path>> subscriber;

  private volatile boolean completed = false;
  private volatile boolean shouldComplete = false;

  private @NonNull Path getRelativePath(@NonNull MusicFile file) {
    return Path.of(formatter.format(file));
  }

  @Override
  public void request(long n) {
    if (completed) return;

    while (!completed && n-- > 0) {
      synchronized (queue) {
        while (queue.isEmpty() && !completed)
          try {
            queue.wait();
          } catch (InterruptedException e) {
            e.printStackTrace(); // todo
          }

        consume(Objects.requireNonNull(queue.poll())); // todo

        if (shouldComplete && queue.isEmpty()) {
          completed = true;
        }
        queue.notify();
      }
    }
  }

  private void consume(@NonNull Path path) {
    try {
      var file = factory.make(path);
      var pair = Pair.of(path, getRelativePath(file));

      if (!pair.getLeft().equals(pair.getRight()))
        futures.add(executor.submit(() -> subscriber.onNext(pair)));
      else
        logger.info(
            String.format(
                "%s is already sorted, ignoring.", path.getName(path.getNameCount() - 1)));
      // request(1);
    } catch (MusicFileException e) {
      logger.info(
          String.format("« %s » was not a music file.", path.getName(path.getNameCount() - 1)));
      // request(1); todo
    }
  }

  @Override
  public void cancel() {
    completed = true;
    futures.forEach(future -> future.cancel(true));
    logger.debug("Transponder canceling.");
  }

  public void signalComplete() {
    shouldComplete = true;
  }
}
