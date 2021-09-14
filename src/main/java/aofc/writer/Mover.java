package aofc.writer;

import aofc.utils.FileUtils;
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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Mover implements Consumer<Pair<Path, Path>> {
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private static final int BATCH_QUANTITY = 100;
  private static final AtomicLong handled = new AtomicLong(0);

  private final FileExistsMode fileExistsMode;
  private final MoveMode moveMode;

  @Override
  public void accept(@NonNull Pair<Path, Path> paths) {
    var finalDestination = paths.getRight();
    try {
      logger.debug(String.format("Received « %s »", paths));

      Files.createDirectories(finalDestination.getParent());
      if (moveMode == MoveMode.MOVE) moveFile(finalDestination, paths);
      else copyFile(finalDestination, paths);

      logger.debug(
          "Finished {} of « {} ».",
          moveMode.toString(),
          FileUtils.getShortName(finalDestination, 3));
    } catch (FileAlreadyExistsException e) {
      logger.info("« {} » already exists, skipping.", FileUtils.getShortName(finalDestination, 1));
    } catch (IOException e) {
      // fixme
    } finally {
      if (handled.incrementAndGet() % BATCH_QUANTITY == 0)
        logger.info("Handled {}-th file.", handled.get());
    }
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
}
