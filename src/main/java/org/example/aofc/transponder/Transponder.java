package org.example.aofc.transponder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.reader.exception.MusicFileException;
import org.example.aofc.utils.SyncingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transponder implements Flow.Processor<Path, Pair<Path, Path>> {
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private static final int INITIAL_REQUEST_SIZE = 25;

  private final MusicFileFactory factory = new MusicFileFactory();
  private final SyncingQueue<Pair<Path, Path>> queue = new SyncingQueue<>();
  private final List<Future<?>> futures = new ArrayList<>();

  private final SpecificationFormatter formatter;
  private final ExecutorService executor;

  private Flow.Subscription subscription;
  private Flow.Subscriber<? super Pair<Path, Path>> subscriber;
  private boolean subscriptionCompleted = false;

  private @NonNull Path getRelativePath(@NonNull IMusicFile file) {
    return Path.of(formatter.format(file));
  }

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    this.subscription = subscription;
    executor.submit(() -> subscription.request(INITIAL_REQUEST_SIZE));
  }

  @Override
  public void onNext(@NonNull Path path) {
    if (subscriber != null) handleIt(path);
    else queueIt(path);

    executor.submit(() -> subscription.request(1));
  }

  private void handleIt(@NonNull Path path) {
    synchronized (queue) {
      while (!queue.isEmpty()) subscriber.onNext(queue.pop().orElseThrow());
      queue.notify();
    }

    try {
      var file = factory.make(path);
      futures.add(executor.submit(() -> subscriber.onNext(Pair.of(path, getRelativePath(file)))));
    } catch (MusicFileException e) {
      logger.info(
          String.format("« %s » was not a music file.", path.getName(path.getNameCount() - 1)));
    }
  }

  private void queueIt(@NonNull Path path) {
    synchronized (queue) {
      queue.push(Pair.of(path, getRelativePath(factory.make(path))));
      queue.notify();
    }
  }

  @Override
  public void onError(@NonNull Throwable throwable) {
    futures.forEach(future -> future.cancel(true));
    logger.error(throwable.getMessage());
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    subscriptionCompleted = true;
    logger.info("Flagger completed");
  }

  @Override
  public void subscribe(Flow.Subscriber<? super Pair<Path, Path>> subscriber) {
    if (this.subscriber != null) throw new UnsupportedOperationException();
    this.subscriber = subscriber;
  }
}
