package aofc.formatter.provider;

import aofc.formatter.provider.exception.CouldNotParseDateException;
import aofc.reader.MusicTags;
import aofc.utils.DateUtils;
import aofc.utils.exception.WrongDateFormatException;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

public class TagProviderMapper {
  private static final Logger logger = LoggerFactory.getLogger(TagProviderMapper.class);

  // todo allow for this customization
  private static final int DIGIT_PAD_SIZE = 3;
  private static final String DIGIT_PAD_CHAR = "0";

  // todo allow more customization
  public static TagProvider getProviderFor(@NonNull MusicTags tag) {
    if (tag == MusicTags.DATE)
      return iMusicFile -> {
        var dateTag = iMusicFile.getDateTag();
        if (dateTag.isEmpty()) return dateTag;
        try {
          return DateUtils.getProperDate(dateTag.get());
        } catch (WrongDateFormatException e) {
          throw new CouldNotParseDateException(dateTag.get(), e);
        }
      };

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
