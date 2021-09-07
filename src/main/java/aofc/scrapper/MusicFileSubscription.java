package aofc.scrapper;

import lombok.NonNull;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

public class MusicFileSubscription implements Flow.Subscription {
  private final Queue<Path> queue = new LinkedList<>();

  private final Flow.Subscriber<? super Path> subscriber;
  private final ExecutorService executor;
  private final FileScrapper fileScrapper;
  private final Thread scrapperThread;

  private volatile boolean completed = false;

  public MusicFileSubscription(
      @NonNull Flow.Subscriber<? super Path> subscriber,
      @NonNull ExecutorService executor,
      @NonNull Path origin) {
    this.subscriber = subscriber;
    this.executor = executor;

    this.fileScrapper = new FileScrapper(queue, origin);
    this.scrapperThread = new Thread(this.fileScrapper);
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
          } catch (InterruptedException e) {
            e.printStackTrace(); // todo use subscriber#onError
          }
        }

        if (!queue.isEmpty()) {
          var request = queue.poll();
          executor.execute(() -> subscriber.onNext(request));
        }
        queue.notifyAll();
      }
    }

    if (isCompleted()) subscriber.onComplete();
  }

  private boolean isCompleted() {
    if (queue.isEmpty() && fileScrapper.isCompleted()) completed = true;
    return completed;
  }

  @Override
  public void cancel() {
    fileScrapper.setCompleted(true);
    completed = true;
    subscriber.onError(new RuntimeException("cancelled"));
  }
}
