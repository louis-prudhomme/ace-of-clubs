package org.example.aofc.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class Mover implements Flow.Subscriber<Pair<Path, Path>> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int INITIAL_REQUEST_SIZE = 25;

  private final Path destination;
  private final MoveMode mode;

  private Flow.Subscription subscription;

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    (this.subscription = subscription).request(INITIAL_REQUEST_SIZE);
  }

  @Override
  public void onNext(@NonNull Pair<Path, Path> paths) {
    var finalDestination = destination.resolve(paths.getRight());

    try {
      Files.createDirectories(finalDestination.getParent());

      if (mode == MoveMode.REPLACE_EXISTING)
        Files.move(paths.getLeft(), finalDestination, StandardCopyOption.REPLACE_EXISTING);
      else Files.move(paths.getLeft(), finalDestination);

      logger.info(String.format("Moved %s", finalDestination.toString()));
    } catch (IOException e) {
      onError(e);
    }

    subscription.request(1);
  }

  @Override
  public void onError(@NonNull Throwable throwable) {
    subscription.cancel();
    logger.error(throwable.getMessage());
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    logger.debug("Transponder completed");
  }
}
