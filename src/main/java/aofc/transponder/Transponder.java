package aofc.transponder;

import aofc.formatter.SpecificationFormatter;
import aofc.formatter.provider.exception.TagProviderException;
import aofc.reader.MusicFileFactory;
import aofc.reader.exception.MusicFileException;
import aofc.utils.Transdoer;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

public class Transponder extends Transdoer<Path, Pair<Path, Path>> {
  private final MusicFileFactory factory = new MusicFileFactory();

  private final SpecificationFormatter formatter;
  private final Path destination;

  public Transponder(
      @NonNull ExecutorService executor,
      @NonNull Queue<Path> queue,
      @NonNull Flow.Subscriber<? super Pair<Path, Path>> subscriber,
      @NonNull SpecificationFormatter formatter,
      @NonNull Path destination) {
    super(executor, queue, subscriber);
    this.formatter = formatter;
    this.destination = destination;
  }

  @Override
  protected void consume(@NonNull Path path) {
    boolean sent = false;
    try {
      var file = factory.make(path);
      var filePath = formatter.format(destination, file);
      var pair = Pair.of(path, filePath);

      if (!pair.getLeft().equals(pair.getRight())) {
        futures.add(executor.submit(() -> subscriber.onNext(pair)));
        sent = true;
      } else logger.trace("{} is already sorted, ignoring.", path.getFileName().toString());
    } catch (MusicFileException e) {
      logger.info(
          "« {} » was not a music file ({}).", path.getFileName().toString(), e.getMessage());
    } catch (TagProviderException e) {
      logger.error(
          "Problem reading « {} » tags : {}.", path.getFileName().toString(), e.getMessage());
    } finally {
      if (!sent) futures.add(executor.submit(() -> request(1)));
    }
  }
}
