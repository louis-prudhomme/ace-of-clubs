package aofc.fluxer;

import aofc.transcoder.Transcoder;
import aofc.transponder.Transponder;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;

@AllArgsConstructor
public class FluxFactory {
  @Nullable private final Transcoder transcoder;
  @NotNull private final Transponder transponder;

  public @NotNull IFluxProvider getInstance() {
    if (transcoder == null)
      return baseStream ->
          Flux.fromStream(baseStream)
              .parallel()
              .runOn(Schedulers.parallel())
              .filter(Files::isRegularFile)
              .flatMap(transponder);
    else
      return baseStream ->
          Flux.fromStream(baseStream)
              .parallel()
              .runOn(Schedulers.parallel())
              .filter(Files::isRegularFile)
              .flatMap(transcoder)
              .flatMap(transponder);
  }
}
