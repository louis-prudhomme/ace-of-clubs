package aofc.scrapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.concurrent.Flow;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class FlaggerPublisher implements Flow.Publisher<Path> {
  private final ForkJoinPool pool = new ForkJoinPool();

  private final Path origin;

  private volatile boolean subscribed;

  @Override
  public synchronized void subscribe(@NonNull Flow.Subscriber<? super Path> subscriber) {
    if (subscribed) subscriber.onError(new IllegalStateException());
    else {
      subscribed = true;
      subscriber.onSubscribe(new Flagger(subscriber, pool, origin));
    }
  }

  public boolean await(int timeout) throws InterruptedException {
    if (pool.awaitTermination(timeout, TimeUnit.SECONDS)) return true;

    pool.shutdown();
    return pool.awaitTermination(1, TimeUnit.MILLISECONDS);
  }

  public void submit(@NonNull Flow.Subscriber<? super Path> subscriber) {
    pool.submit(() -> subscribe(subscriber));
  }
}
