package aofc.fluxer;

import aofc.transcoder.Transcoder;
import aofc.transponder.Transponder;
import aofc.writer.Mover;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@AllArgsConstructor
public class Fluxer {
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private final Transcoder transcoder;
  private final Transponder transponder;
  private final Mover mover;
  private final Path origin;

  // todo onComplete
  // todo onError
  public Integer handle(int timeout) {
    try (var files = Files.walk(origin, FileVisitOption.FOLLOW_LINKS)) {
      var future =
          new FutureTask<>(
              () ->
                  new FluxFactory()
                      .getInstance(transcoder, transponder)
                      .provideFor(files)
                      .subscribe(mover));
      future.get(timeout, TimeUnit.SECONDS);
      return 0;
    } catch (ExecutionException | IOException | InterruptedException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      return 1;
    } catch (TimeoutException e) {
      return 1500;
    }
  }
}
