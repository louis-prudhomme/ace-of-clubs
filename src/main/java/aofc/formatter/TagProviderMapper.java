package aofc.formatter;

import aofc.reader.MusicFile;
import aofc.reader.MusicTags;
import aofc.utils.DateUtils;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class TagProviderMapper {
  // todo allow for this customization
  private static final int DIGIT_PAD_SIZE = 3;
  private static final String DIGIT_PAD_CHAR = "0";

  // todo allow more customization
  public static Function<MusicFile, Optional<String>> getProviderFor(@NonNull MusicTags tag) {
    if (tag == MusicTags.DATE)
      return iMusicFile ->
          iMusicFile.getDateTag().map(DateUtils::getProperDate).orElseThrow(); // todo orElseThrow
    if (tag == MusicTags.TRACK) return file -> formatNumbers(file::getTrackTag);
    if (tag == MusicTags.DISC) return file -> formatNumbers(file::getDiscTag);
    return iMusicFile -> iMusicFile.getTag(tag);
  }

  private static @NonNull Optional<String> formatNumbers(
      @NonNull Supplier<Optional<String>> getter) {
    return getter.get().map(TagProviderMapper::splitOutOf).map(TagProviderMapper::padLeft);
  }

  private static @NonNull String padLeft(@NonNull String toPad) {
    return StringUtils.leftPad(toPad, DIGIT_PAD_SIZE, DIGIT_PAD_CHAR);
  }

  private static @NonNull String splitOutOf(@NonNull String toSplit) {
    return toSplit.contains("/") ? toSplit.split("/")[0] : toSplit;
  }
}
