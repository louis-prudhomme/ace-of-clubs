package aofc.fluxer;

import aofc.writer.Mover;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Supplier;

@AllArgsConstructor
public class Fluxer {
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private final Supplier<IFluxProvider> factory;
  private final Mover mover;
  private final Path origin;

  // todo onComplete
  // todo onError
  public Integer handle(long timeout) {
    try (var files = Files.walk(origin, FileVisitOption.FOLLOW_LINKS)) {
      factory.get().provideFor(files).doOnNext(mover).then().block(Duration.ofSeconds(timeout));
      logger.info("Finished");
      return 0;
    } catch (IOException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      return 1;
    }
  }
}
