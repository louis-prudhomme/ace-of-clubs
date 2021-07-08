package aofc.scrapper;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;

@RequiredArgsConstructor
public class Scrapper implements Runnable {
  private final Logger logger = LoggerFactory.getLogger("aofc");
  private static final int PRODUCING_RATE = 20;

  private final Queue<Path> queue;
  private final Path origin;

  @Getter @Setter private volatile boolean completed = false;

  @Override
  public void run() {
    try (var files = Files.walk(origin, FileVisitOption.FOLLOW_LINKS)) {
      files
          .filter(Files::isRegularFile)
          .forEach(
              item -> {
                if (completed) files.close();
                try {
                  produce(item);
                } catch (InterruptedException e) {
                  files.close();
                }
              });
    } catch (IOException e) {
      logger.error(e.getMessage());
      throw new RuntimeException(e);
    } finally {
      completed = true;
    }
  }

  private void produce(@NonNull Path item) throws InterruptedException {
    synchronized (queue) {
      while (!completed && queue.size() > PRODUCING_RATE) {
        queue.wait();
      }
      queue.offer(item);
      queue.notify();
    }
  }
}
