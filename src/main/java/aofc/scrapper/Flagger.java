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

  private volatile boolean completed = false;

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
            // futures.removeIf(Future::isDone); todo
            queue.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

        if (!queue.isEmpty()) {
          var request = queue.poll();
          futures.add(executor.submit(() -> subscriber.onNext(request)));
        }
        queue.notifyAll();
      }
    }

    if (isCompleted()) subscriber.onComplete();
  }

  private boolean isCompleted() {
    if (queue.isEmpty() && scrapper.isCompleted()) completed = true;
    return completed;
  }

  @Override
  public void cancel() {
    scrapper.setCompleted(true);
    completed = true;
    subscriber.onError(new RuntimeException("cancelled"));
    futures.forEach(future -> future.cancel(true));
  }
}
