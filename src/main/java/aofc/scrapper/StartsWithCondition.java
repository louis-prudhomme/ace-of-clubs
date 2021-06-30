package aofc.scrapper;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.file.FileSystems;
import java.util.function.Function;

@RequiredArgsConstructor
public class StartsWithCondition implements IScrapperQueryProvider {
  private final String destinationPath;
  private final String startsWith;

  @Override
  public @NonNull Function<String, Boolean> getQuery() {
    return s ->
        s.startsWith(
            String.format(
                "%s%s%s", destinationPath, FileSystems.getDefault().getSeparator(), startsWith));
  }
}
