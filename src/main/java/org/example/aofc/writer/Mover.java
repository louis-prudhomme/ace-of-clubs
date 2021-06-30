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

  private static final int INITIAL_REQUEST_SIZE = 10;

  private final Path destination;
  private final FileExistsMode fileExistsMode;
  private final MoveMode moveMode;

  private Flow.Subscription subscription;

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    this.subscription = subscription;
    subscription.request(INITIAL_REQUEST_SIZE);
  }

  @Override
  public void onNext(@NonNull Pair<Path, Path> paths) {
    var finalDestination = paths.getRight();
    try {
      Files.createDirectories(finalDestination.getParent());
      if (moveMode == MoveMode.MOVE) moveFile(finalDestination, paths);
      else copyFile(finalDestination, paths);

      logger.debug(
          String.format(
              "Finished %s of « %s. »", moveMode.toString(), getShortName(finalDestination, 3)));
    } catch (FileAlreadyExistsException e) {
      logger.info(String.format("%s already exists, skipping.", getShortName(finalDestination, 1)));
    } catch (IOException e) {
      onError(e);
    } finally {
      subscription.request(1);
    }
  }

  private String getShortName(@NonNull Path path, int nb) {
    return path.subpath(path.getNameCount() - nb, path.getNameCount()).toString();
  }

  private void moveFile(@NonNull Path finalDestination, @NonNull Pair<Path, Path> paths)
      throws IOException {
    if (fileExistsMode == FileExistsMode.REPLACE_EXISTING)
      Files.move(paths.getLeft(), finalDestination, StandardCopyOption.REPLACE_EXISTING);
    else Files.move(paths.getLeft(), finalDestination);
  }

  private void copyFile(@NonNull Path finalDestination, @NonNull Pair<Path, Path> paths)
      throws IOException {
    if (fileExistsMode == FileExistsMode.REPLACE_EXISTING)
      Files.copy(paths.getLeft(), finalDestination, StandardCopyOption.REPLACE_EXISTING);
    else Files.copy(paths.getLeft(), finalDestination);
  }

  /**
   * throw in this method is undefined behavior and will crash the worker ; printing stacktrace
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
    logger.info("Transponder completed");
  }
}
