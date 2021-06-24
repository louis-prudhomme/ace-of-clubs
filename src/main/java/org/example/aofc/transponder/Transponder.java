package org.example.aofc.transponder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.utils.DateUtils;
import org.example.aofc.utils.FileUtils;
import org.example.aofc.utils.SyncingQueue;

import java.nio.file.Path;
import java.util.concurrent.Flow;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transponder implements Flow.Processor<Path, Pair<Path, Path>> {
  private static final int REQUEST_SIZE = 25;
  private static final int TRACK_PAD_SIZE = 3;
  private static final String TRACK_PAD_CHAR = "0";

  private final MusicFileFactory factory = new MusicFileFactory();
  private final SyncingQueue<Pair<Path, Path>> queue = new SyncingQueue<>();

  private Flow.Subscription subscription;
  private Flow.Subscriber<? super Pair<Path, Path>> subscriber;

  private @NonNull Path getRelativePath(@NonNull IMusicFile file) {
    var sanitizer =
        new Sanitizer(
            file.getAlbumArtistTag(),
            file.getDateTag().map(DateUtils::getDate),
            file.getAlbumTag(),
            file.getTrackTag().map(s -> StringUtils.leftPad(s, TRACK_PAD_SIZE, TRACK_PAD_CHAR)),
            file.getTitleTag(),
            FileUtils.getExtension(file.getPath()));

    return Path.of(
        String.format(
            "%s/%s – %s/%s – %s.%s",
            (Object[])
                sanitizer.santizeBulk())); // todo allow custom formatting in command line option
    // todo refactor fkin ugly ass cast
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
