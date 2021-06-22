package org.example.aofc.scrapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;

@RequiredArgsConstructor
public class Scrapper implements Flow.Publisher<String> {
  private final ExecutorService executor;
  private final Path path;
  private boolean subscribed;

  @Override
  public synchronized void subscribe(Flow.Subscriber<? super String> subscriber) {
    if (subscribed) subscriber.onError(new IllegalStateException()); // only one allowed
    else {
      subscribed = true;
      subscriber.onSubscribe(new MusicFileSubscription(subscriber, executor, path));
    }
  }

  @RequiredArgsConstructor
  static class MusicFileSubscription implements Flow.Subscription {
    private final Flow.Subscriber<? super String> subscriber;
    private final ExecutorService executor;
    private final Path path;

    private final List<Future<?>> futures = new ArrayList<>(); // to allow cancellation
    private boolean completed;

    @Override
    public synchronized void request(long n) {
      if (completed) return;
      if (n < Long.MAX_VALUE) throw new UnsupportedOperationException();

      try {
        Files.walk(path, FileVisitOption.FOLLOW_LINKS)
            .filter(Files::isRegularFile)
            .map(Path::toString)
            .forEach(this::produce);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private synchronized void produce(@NonNull String path) {
      futures.add(executor.submit(() -> subscriber.onNext(path)));
    }

    @Override
    public synchronized void cancel() {
      completed = true;
      if (!futures.isEmpty()) futures.forEach(future -> future.cancel(false));
    }
  }
}
