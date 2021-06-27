package org.example.aofc.scrapper;

import lombok.NonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;

public class Flagger implements Flow.Subscription {
  private final Flow.Subscriber<? super Path> subscriber;
  private final ExecutorService executor;
  private final Scrapper scrapper;

  private final List<Future<?>> futures = new ArrayList<>();
  private final Thread scrapperThread;
  private boolean completed = false;

  public Flagger(
      @NonNull Flow.Subscriber<? super Path> subscriber,
      @NonNull ExecutorService executor,
      @NonNull Path origin) {
    this.subscriber = subscriber;
    this.executor = executor;

    this.scrapper = new Scrapper(origin, () -> completed = true);
    this.scrapperThread = new Thread(this.scrapper);
    this.scrapperThread.start();
  }

  @Override
  public void request(long n) {
    if (completed) return;

    while (!completed && n-- != 0) {
      synchronized (scrapper.getQueue()) {
        while (scrapper.getQueue().isEmpty() && !completed) {
          try {
            scrapper.getQueue().wait();
          } catch (InterruptedException ignored) {
          }
        }

        produce(scrapper.getQueue().pop().orElseThrow());
        scrapper.getQueue().notify();
      }
    }
  }

  private void produce(@NonNull Path path) {
    futures.add(executor.submit(() -> subscriber.onNext(path)));
  }

  @Override
  public void cancel() {
    scrapperThread.interrupt();
    futures.forEach(future -> future.cancel(true));
  }
}
