package aofc.transcoder;

import aofc.transponder.EncodingCodecs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class TranscoderProcessor implements Flow.Processor<Path, Path> {
  private static final int INITIAL_REQUEST_SIZE = 20;
  private static final int PRODUCING_RATE = 100;
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private final Queue<Path> queue = new LinkedList<>();
  private final ForkJoinPool pool = new ForkJoinPool();

  private final EncodingCodecs format;

  private TranscoderSubscription transcoderSubscription;
  private Flow.Subscription subscription;
  private boolean shouldComplete = false;

  @Override
  public void subscribe(Flow.Subscriber<? super Path> subscriber) {
    if (this.transcoderSubscription != null) throw new UnsupportedOperationException();

    this.transcoderSubscription = new TranscoderSubscription(pool, queue, subscriber, format);
    subscriber.onSubscribe(transcoderSubscription);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    this.subscription = subscription;
  }

  @Override
  public void onNext(@NonNull Path item) {
    synchronized (queue) {
      while (queue.size() > PRODUCING_RATE)
        try {
          queue.wait();
        } catch (InterruptedException e) {
          e.printStackTrace(); // todo
        }

      queue.offer(item);
      queue.notify();
    }

    if (!isCompleted() && !shouldComplete) subscription.request(1);
    else transcoderSubscription.signalComplete();
  }

  private boolean isCompleted() {
    return shouldComplete && queue.isEmpty();
  }

  @Override
  public void onError(Throwable throwable) {
    logger.error(throwable.getMessage());
    transcoderSubscription.cancel();
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    shouldComplete = true;
    logger.debug("TranscoderProcessor completed.");
  }

  public void submit(@NonNull Flow.Subscriber<? super Path> subscriber) {
    pool.submit(() -> subscribe(subscriber));
  }

  public boolean await(int timeout) throws InterruptedException {
    if (timeout == Integer.MAX_VALUE) {
      if (pool.awaitQuiescence(timeout, TimeUnit.SECONDS)) return true;
    } else if (pool.awaitTermination(timeout, TimeUnit.SECONDS)) return true;

    pool.shutdown();
    return pool.awaitTermination(1, TimeUnit.MILLISECONDS);
  }
}
