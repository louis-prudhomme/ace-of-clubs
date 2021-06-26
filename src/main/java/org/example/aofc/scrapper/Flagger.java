package org.example.aofc.scrapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.function.Function;

@RequiredArgsConstructor
public class Flagger implements Flow.Publisher<Path> {
  private final ExecutorService executor;
  private final Path origin;
  private final Function<Path, Boolean> filterCriteria;

  private boolean subscribed;

  @Override
  public synchronized void subscribe(@NonNull Flow.Subscriber<? super Path> subscriber) {
    if (subscribed) subscriber.onError(new IllegalStateException()); // only one allowed
    else {
      subscribed = true;
      subscriber.onSubscribe(
          new MusicFileSubscription(subscriber, executor, origin, filterCriteria));
    }
  }

  static class MusicFileSubscription implements Flow.Subscription {
    private final Flow.Subscriber<? super Path> subscriber;
    private final ExecutorService executor;
    private final Scrapper scrapper;

    private final List<Future<?>> futures = new ArrayList<>(); // to cancellation
    private final Thread scrapperThread;
    private boolean completed = false;

    public MusicFileSubscription(
        @NonNull Flow.Subscriber<? super Path> subscriber,
        @NonNull ExecutorService executor,
        @NonNull Path origin,
        @NonNull Function<Path, Boolean> filterCriteria) {
      this.subscriber = subscriber;
      this.executor = executor;

      this.scrapper = new Scrapper(origin, filterCriteria, () -> completed = true);
      this.scrapperThread = new Thread(this.scrapper);
      this.scrapperThread.start();
    }

    @Override
    public synchronized void request(long n) {
      if (completed) return;

      while (!completed && n-- != 0) {
        synchronized (scrapper.getQueue()) {
          while (scrapper.getQueue().isEmpty() && !completed) {
            try {
              scrapper.getQueue().wait();
            } catch (InterruptedException e) {
              scrapperThread.interrupt();
            }
          }

          produce(scrapper.getQueue().pop().orElseThrow());
          scrapper.getQueue().notify();
        }
      }
    }

    private synchronized void produce(@NonNull Path path) {
      futures.add(executor.submit(() -> subscriber.onNext(path)));
    }

    @Override
    public synchronized void cancel() {
      scrapperThread.interrupt();
      futures.forEach(future -> future.cancel(true));
    }
  }
}
