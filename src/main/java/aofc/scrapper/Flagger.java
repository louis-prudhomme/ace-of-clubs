package aofc.scrapper;

import lombok.NonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;

public class Flagger implements Flow.Subscription {
  private final Queue<Path> queue = new LinkedList<>();
  private final List<Future<?>> futures = new ArrayList<>();

  private final Flow.Subscriber<? super Path> subscriber;
  private final ExecutorService executor;
  private final Scrapper scrapper;
  private final Thread scrapperThread;

  public Flagger(
      @NonNull Flow.Subscriber<? super Path> subscriber,
      @NonNull ExecutorService executor,
      @NonNull Path origin) {
    this.subscriber = subscriber;
    this.executor = executor;

    this.scrapper = new Scrapper(queue, origin);
    this.scrapperThread = new Thread(this.scrapper);
    this.scrapperThread.start();
  }

  @Override
  public void request(long n) {
    if (isCompleted()) subscriber.onComplete();

    while (!isCompleted() && n-- > 0) {
      synchronized (queue) {
        while (queue.isEmpty() && !isCompleted()) {
          try {
            queue.wait();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        if (!queue.isEmpty()) {
          var request = queue.poll();
          futures.add(executor.submit(() -> subscriber.onNext(request)));
        }
        queue.notify();
      }
    }
  }

  private boolean isCompleted() {
    return queue.isEmpty() && scrapper.isCompleted();
  }

  @Override
  public void cancel() {
    subscriber.onError(new RuntimeException("cancelled"));
    scrapperThread.interrupt();
    futures.forEach(future -> future.cancel(true));
  }
}
