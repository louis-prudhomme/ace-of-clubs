package aofc.transponder;

import aofc.formatter.SpecificationFormatter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class RenamingProcessor implements Flow.Processor<Path, Pair<Path, Path>> {
  private static final int INITIAL_REQUEST_SIZE = 20;
  private static final int PRODUCING_RATE = 100;
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private final Queue<Path> queue = new LinkedList<>();
  private final ForkJoinPool pool = new ForkJoinPool();

  private final SpecificationFormatter formatter;
  private final Path destination;

  private RenamingSubscription renamingSubscription;
  private Flow.Subscription subscription;
  private boolean shouldComplete = false;

  @Override
  public void subscribe(@NonNull Flow.Subscriber<? super Pair<Path, Path>> subscriber) {
    if (this.renamingSubscription != null) throw new UnsupportedOperationException();

    this.renamingSubscription =
        new RenamingSubscription(pool, queue, subscriber, formatter, destination);
    subscriber.onSubscribe(renamingSubscription);
  }

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    this.subscription = subscription;
  }

  @Override
  public void onNext(@NonNull Path path) {
    synchronized (queue) {
      while (queue.size() > PRODUCING_RATE)
        try {
          queue.wait();
        } catch (InterruptedException e) {
          e.printStackTrace(); // todo
        }

      queue.offer(path);
      queue.notify();
    }

    if (!isCompleted()) subscription.request(1);
    else renamingSubscription.signalComplete();
  }

  private boolean isCompleted() {
    return shouldComplete && queue.isEmpty();
  }

  @Override
  public void onError(Throwable throwable) {
    logger.error(throwable.getMessage());
    renamingSubscription.cancel();
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    shouldComplete = true;
    logger.debug("TransponderProcessor completed.");
  }

  public boolean await(int timeout) throws InterruptedException {
    if (timeout == Integer.MAX_VALUE) {
      if (pool.awaitQuiescence(timeout, TimeUnit.SECONDS)) return true;
    } else if (pool.awaitTermination(timeout, TimeUnit.SECONDS)) return true;

    pool.shutdown();
    return pool.awaitTermination(1, TimeUnit.MILLISECONDS);
  }

  public void submit(@NonNull Flow.Subscriber<? super Pair<Path, Path>> subscriber) {
    pool.submit(() -> subscribe(subscriber));
  }
}
