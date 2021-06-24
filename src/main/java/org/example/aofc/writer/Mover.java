package org.example.aofc.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class Mover implements Flow.Subscriber<Pair<Path, Path>> {
  private static final int REQUEST_SIZE = 25;

  private final Path destination;
  private final MoveMode mode;

  private Flow.Subscription subscription;

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    (this.subscription = subscription).request(Long.MAX_VALUE);
  }

  @Override
  @SneakyThrows // todo remove
  public void onNext(@NonNull Pair<Path, Path> paths) {
    Path finalDestination = destination.resolve(paths.getRight());
    Files.createDirectories(finalDestination.getParent());

    if (mode == MoveMode.REPLACE_EXISTING)
      Files.move(paths.getLeft(), finalDestination, StandardCopyOption.REPLACE_EXISTING);
    else Files.move(paths.getLeft(), finalDestination);

    subscription.request(REQUEST_SIZE);
  }

  @Override
  public void onError(@NonNull Throwable throwable) {
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    System.out.println("Finished");
  }
}
