package aofc.transponder;

import aofc.formatter.SpecificationFormatter;
import aofc.formatter.exception.SpecificationFormattingException;
import aofc.reader.MusicFileFactory;
import aofc.reader.exception.MusicFileException;
import aofc.utils.FileUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Transponder implements Flow.Subscription {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final MusicFileFactory factory = new MusicFileFactory();
  private final List<Future<?>> futures = new ArrayList<>();

  private final SpecificationFormatter formatter;
  private final ExecutorService executor;
  private final Queue<Path> queue;
  private final Flow.Subscriber<? super Pair<Path, Path>> subscriber;
  private final Path destination;

  private volatile boolean shouldComplete = false;
  private volatile boolean completed = false;

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

  private boolean isCompleted() {
    if (shouldComplete && queue.isEmpty()) completed = true;
    return completed;
  }

  private void consume(@NonNull Path path) {
    try {
      var file = factory.make(path);
      var filePath = formatter.format(destination, file);
      var pair = Pair.of(path, filePath);

      if (!pair.getLeft().equals(pair.getRight()))
        futures.add(executor.submit(() -> subscriber.onNext(pair)));
      else {
        logNRequest(logger::info, "%s is already sorted, ignoring.", path.getFileName().toString());
      }
    } catch (MusicFileException e) {
      logNRequest(
          logger::info,
          "« %s » was not a music file (%s).",
          path.getFileName().toString(),
          e.getMessage());
    } catch (SpecificationFormattingException e) {
      logNRequest(
          logger::error,
          "Problem reading « %s » tags : %s (%s).",
          path.getFileName().toString(),
          e.getMessage(),
          FileUtils.getShortName(path, 3));
    }
  }

  private void logNRequest(
      @NonNull Consumer<String> loggat, @NonNull String toFormat, String... args) {
    loggat.accept(String.format(toFormat, (Object[]) args));
    futures.add(executor.submit(() -> request(1)));
  }

  @Override
  public void cancel() {
    completed = true;
    futures.forEach(future -> future.cancel(true)); // todo useful ?
    logger.info("Transponder canceling.");
  }

  public void signalComplete() {
    shouldComplete = true;
  }
}
