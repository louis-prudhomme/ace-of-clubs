package aofc.transponder;

import aofc.formatter.SpecificationFormatter;
import aofc.formatter.provider.exception.TagProviderException;
import aofc.reader.MusicFileFactory;
import aofc.reader.exception.MusicFileException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.function.Function;

@AllArgsConstructor
public class Transponder implements Function<Path, Flux<Pair<Path, Path>>> {
  private final Logger logger = LoggerFactory.getLogger("aofc");
  private final MusicFileFactory factory = new MusicFileFactory();

  private final SpecificationFormatter formatter;
  private final Path destination;

  @Override
  public @NonNull Flux<Pair<Path, Path>> apply(@NonNull Path path) {
    try {
      var file = factory.make(path);
      var filePath = formatter.format(destination, file);
      var pair = Pair.of(path, filePath);

      if (!pair.getLeft().equals(pair.getRight())) {
        return Flux.just(pair);
      } else {
        logger.trace("{} is already sorted, ignoring.", path.getFileName().toString());
        return Flux.empty();
      }
    } catch (MusicFileException e) {
      logger.info(
          "« {} » was not a music file ({}).", path.getFileName().toString(), e.getMessage());
      throw new NotImplementedException();
    } catch (TagProviderException e) {
      logger.error(
          "Problem reading « {} » tags : {}.", path.getFileName().toString(), e.getMessage());
      throw new NotImplementedException();
    } finally {
      // todo handle error
    }
  }
}
