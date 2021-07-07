package aofc.formatter.provider;

import aofc.formatter.provider.exception.TagProviderException;
import aofc.reader.MusicFile;
import lombok.NonNull;

import java.util.Optional;

@FunctionalInterface
public interface TagProvider {
  @NonNull
  Optional<String> apply(@NonNull MusicFile musicFile) throws TagProviderException;
}
