package org.example.aofc.transponder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.example.aofc.formatter.SpecificationFormatter;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.utils.SyncingQueue;

import java.nio.file.Path;
import java.util.concurrent.Flow;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transponder implements Flow.Processor<Path, Pair<Path, Path>> {
  private static final int REQUEST_SIZE = 25;

  private final MusicFileFactory factory = new MusicFileFactory();
  private final SyncingQueue<Pair<Path, Path>> queue = new SyncingQueue<>();
  private final SpecificationFormatter formatter;

  private Flow.Subscription subscription;
  private Flow.Subscriber<? super Pair<Path, Path>> subscriber;

  private @NonNull Path getRelativePath(@NonNull IMusicFile file) {
    var t = formatter.format(file);
    return Path.of(formatter.format(file));
  }

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    (this.subscription = subscription).request(REQUEST_SIZE);
  }

  @Override
  public void onNext(@NonNull Path path) {
    if (subscriber != null) handleIt(path);
    else queueIt(path);

    subscription.request(REQUEST_SIZE);
  }

  private void handleIt(@NonNull Path path) {
    synchronized (queue) {
      while (!queue.isEmpty()) subscriber.onNext(queue.pop().orElseThrow());
    }

    subscriber.onNext(Pair.of(path, getRelativePath(factory.make(path))));
  }

  private void queueIt(@NonNull Path path) {
    synchronized (queue) {
      queue.push(Pair.of(path, getRelativePath(factory.make(path))));
      queue.notify();
    }
  }

  @Override
  public void onError(@NonNull Throwable throwable) {
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {}

  @Override
  public void subscribe(Flow.Subscriber<? super Pair<Path, Path>> subscriber) {
    if (this.subscriber != null) throw new UnsupportedOperationException();
    this.subscriber = subscriber;
  }
}
