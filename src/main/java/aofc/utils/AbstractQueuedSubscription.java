package aofc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

public abstract class AbstractQueuedSubscription<T, R> implements Flow.Subscription {
  protected final Logger logger = LoggerFactory.getLogger("aofc");

  protected final ExecutorService executor;
  protected final Queue<T> queue;
  protected final Flow.Subscriber<? super R> subscriber;

  protected volatile boolean shouldComplete = false;
  protected volatile boolean completed = false;

  public AbstractQueuedSubscription(
      ExecutorService executor, Queue<T> queue, Flow.Subscriber<? super R> subscriber) {
    this.executor = executor;
    this.queue = queue;
    this.subscriber = subscriber;
  }

  protected abstract void consume(T consumat);

  @Override
  public void request(long n) {
    if (isCompleted()) subscriber.onComplete();

    while (!isCompleted() && n-- > 0) {
      synchronized (queue) {
        while (queue.isEmpty() && !isCompleted())
          try {
            queue.wait();
          } catch (InterruptedException e) {
            e.printStackTrace(); // todo
          }

        if (!queue.isEmpty()) consume(queue.poll());
        queue.notify();
      }
    }
  }

  protected boolean isCompleted() {
    if (shouldComplete && queue.isEmpty()) completed = true;
    return completed;
  }

  public void signalComplete() {
    shouldComplete = true;
  }

  @Override
  public void cancel() {
    completed = true;
    logger.info("Canceling.");
  }
}