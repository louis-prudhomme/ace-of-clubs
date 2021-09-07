package aofc.fluxer;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.ParallelFlux;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface IFluxProvider {
  ParallelFlux<Pair<Path, Path>> provideFor(@NotNull Stream<Path> baseStream);
}
