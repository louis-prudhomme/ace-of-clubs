package aofc.transponder;

import aofc.formatter.SpecificationFormatter;
import aofc.formatter.provider.exception.TagProviderException;
import aofc.reader.MusicFileFactory;
import aofc.reader.exception.MusicFileException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.jaudiotagger.audio.exceptions.CannotReadException;
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
        logger.info("« {} » is already sorted, ignoring.", path.getFileName().toString());
        return Flux.empty();
      }
    } catch (MusicFileException e) {
      if (path.toString().endsWith("ogg") && e.getCause() instanceof CannotReadException) {
        logger.warn(
            "« {} » vorbis file was badly formatted, trying to trick it anyway ({}).",
            path.getFileName().toString(),
            e.getMessage());
        return Flux.just(Pair.of(path, formatter.formatOgg(destination, path)));
      }
      logger.warn(
          "« {} » was not a music file ({}).", path.getFileName().toString(), e.getMessage());
      return Flux.empty();
    } catch (TagProviderException e) {
      logger.error(
          "Problem reading « {} » tags : {}.", path.getFileName().toString(), e.getMessage());
      return Flux.empty(); // fixme
    }
  }
}
