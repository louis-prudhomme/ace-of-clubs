package org.example.aofc.scrapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class FlaggerPublisher implements Flow.Publisher<Path> {
  private final ExecutorService executor;
  private final Path origin;

  private volatile boolean subscribed;

  @Override
  public synchronized void subscribe(@NonNull Flow.Subscriber<? super Path> subscriber) {
    if (subscribed) subscriber.onError(new IllegalStateException()); // only one allowed
    else {
      subscribed = true;
      subscriber.onSubscribe(new Flagger(subscriber, executor, origin));
    }
  }
}
