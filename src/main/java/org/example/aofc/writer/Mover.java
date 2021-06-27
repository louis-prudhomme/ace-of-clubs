package org.example.aofc.writer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Flow;

@RequiredArgsConstructor
public class Mover implements Flow.Subscriber<Pair<Path, Path>> {
  private final Logger logger = LoggerFactory.getLogger(getClass());

  private static final int INITIAL_REQUEST_SIZE = 5;

  private final Path destination;
  private final FileExistsMode fileExistsMode;
  private final MoveMode moveMode;

  private Flow.Subscription subscription;

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    this.subscription = subscription;
  }

  @Override
  public void onNext(@NonNull Pair<Path, Path> paths) {
    var finalDestination = destination.resolve(paths.getRight());
    try {
      Files.createDirectories(finalDestination.getParent());
      if (moveMode == MoveMode.MOVE) moveFile(finalDestination, paths);
      else copyFile(finalDestination, paths);
    } catch (IOException e) {
      onError(e);
    }
    subscription.request(1);
  }

  private void moveFile(@NonNull Path finalDestination, @NonNull Pair<Path, Path> paths)
      throws IOException {
    if (fileExistsMode == FileExistsMode.REPLACE_EXISTING)
      Files.move(paths.getLeft(), finalDestination, StandardCopyOption.REPLACE_EXISTING);
    else
      try {
        Files.move(paths.getLeft(), finalDestination);
      } catch (FileAlreadyExistsException e) {
        logFileAlreadyExists(finalDestination);
      }

    logger.debug(String.format("Moved %s.", finalDestination.toString()));
  }

  private void copyFile(@NonNull Path finalDestination, @NonNull Pair<Path, Path> paths)
      throws IOException {
    if (fileExistsMode == FileExistsMode.REPLACE_EXISTING)
      Files.copy(paths.getLeft(), finalDestination, StandardCopyOption.REPLACE_EXISTING);
    else
      try {
        Files.copy(paths.getLeft(), finalDestination);
      } catch (FileAlreadyExistsException e) {
        logFileAlreadyExists(finalDestination);
      }

    logger.debug(String.format("Copied %s.", finalDestination.toString()));
  }

  private void logFileAlreadyExists(@NonNull Path path) {
    logger.error(
        String.format("%s already exists, skipping.", path.getName(path.getNameCount() - 1)));
  }

  /**
   * throw in this method is undefined behavior and will crash the worker ; printint stacktrace
   * necessary for fast debug
   */
  @Override
  public void onError(@NonNull Throwable throwable) {
    logger.error(throwable.getMessage());
    throwable.printStackTrace();
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {
    logger.debug("Transponder completed");
  }

  public void request() {
    this.subscription.request(INITIAL_REQUEST_SIZE);
  }
}
