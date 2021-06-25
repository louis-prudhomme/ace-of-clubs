package org.example.aofc.formatter;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicTags;
import org.example.aofc.utils.DateUtils;

import java.util.Optional;
import java.util.function.Function;

public class TagProviderMapper {
  private static final int TRACK_PAD_SIZE = 3;
  private static final String TRACK_PAD_CHAR = "0";

  // todo allow more customization
  public static Function<IMusicFile, Optional<String>> getProviderFor(@NonNull MusicTags tag) {
    if (tag == MusicTags.DATE)
      return iMusicFile ->
          iMusicFile.getDateTag().map(DateUtils::getProperDate).orElseThrow(); // todo orElseThrow
    if (tag == MusicTags.TRACK)
      return iMusicFile ->
          iMusicFile.getTrackTag().map(s -> StringUtils.leftPad(s, TRACK_PAD_SIZE, TRACK_PAD_CHAR));
    return iMusicFile -> iMusicFile.getTag(tag);
  }
}
