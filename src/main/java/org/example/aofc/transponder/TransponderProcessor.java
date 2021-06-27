package org.example.aofc.transponder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.utils.SyncingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class TransponderProcessor implements Flow.Processor<Path, Pair<Path, Path>> {
  private static final int INITIAL_REQUEST_SIZE = 5;
  private static final int PRODUCING_RATE = 50;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final SyncingQueue<Path> queue = new SyncingQueue<>();

  private final ExecutorService executor;
  private final SpecificationFormatter formatter;

  private Transponder transponder;

  private Flow.Subscription subscription;

  @Override
  public void subscribe(@NonNull Flow.Subscriber<? super Pair<Path, Path>> subscriber) {
    if (this.transponder != null) throw new UnsupportedOperationException();

    this.transponder = new Transponder(formatter, executor, queue, subscriber);
    subscriber.onSubscribe(transponder);
  }

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    this.subscription = subscription;
    executor.submit(() -> subscription.request(INITIAL_REQUEST_SIZE)); // todo
  }

  @Override
  public void onNext(@NonNull Path item) {
    synchronized (queue) {
      while (queue.size() >= PRODUCING_RATE)
        try {
          queue.wait();
        } catch (InterruptedException e) {
          e.printStackTrace(); // todo
        }

      queue.push(item);
      queue.notify();
    }

    executor.submit(() -> subscription.request(1));
  }

  @Override
  public void onError(Throwable throwable) {
    transponder.cancel();
    logger.error(throwable.getMessage());
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    transponder.signalComplete();
    logger.debug("TransponderProcessor completed.");
  }
}
