package aofc.fluxer;

import aofc.writer.Mover;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

@AllArgsConstructor
public class Fluxer {
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private final Supplier<IFluxProvider> factory;
  private final Mover mover;
  private final Path origin;

  private final CountDownLatch latch = new CountDownLatch(1);

  // todo onComplete
  // todo onError
  @SneakyThrows
  public Integer handle() {
    try (var files = Files.walk(origin, FileVisitOption.FOLLOW_LINKS)) {
      factory.get().provideFor(files).subscribe(mover, this::onError, this::onComplete);

      latch.await();
      return 0;
    } catch (IOException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }

  private void onComplete() {
    latch.countDown();
    logger.trace("seiofjqoidjq");
  }

  private void onError(Throwable t) {
    logger.error(t.getMessage());
  }
}
