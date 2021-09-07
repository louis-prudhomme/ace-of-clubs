package aofc.fluxer;

import aofc.transcoder.Transcoder;
import aofc.transponder.Transponder;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Flux;

import java.nio.file.Files;

@AllArgsConstructor
public class FluxFactory {
  public @NotNull IFluxProvider getInstance(
      @Nullable Transcoder transcoder, @NotNull Transponder transponder) {
    if (transcoder == null)
      return baseStream ->
          Flux.fromStream(baseStream).parallel().filter(Files::isRegularFile).flatMap(transponder);
    else
      return baseStream ->
          Flux.fromStream(baseStream)
              .parallel()
              .filter(Files::isRegularFile)
              .flatMap(transcoder)
              .flatMap(transponder);
  }
}
