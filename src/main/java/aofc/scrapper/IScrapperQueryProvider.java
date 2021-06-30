package aofc.scrapper;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

public interface IScrapperQueryProvider {
  @NonNull
  Function<String, Boolean> getQuery();

  @NonNull
  default Function<String, Boolean> chainWith(
      @NonNull Function<String, Boolean> query1, Function<String, Boolean>... others) {
    var queries = new ArrayList<Function<String, Boolean>>();

    queries.add(this.getQuery());
    queries.add(query1);
    queries.addAll(Arrays.asList(others));

    return queries.stream()
        .reduce((cond1, cond2) -> s -> cond1.apply(s) || cond2.apply(s))
        .orElseThrow();
  }
}
