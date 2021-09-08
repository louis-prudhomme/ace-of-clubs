package aofc.reader;

import aofc.formatter.exception.BadMusicTagException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@RequiredArgsConstructor
public enum MusicTags {
  ALBUM("album"),
  ALBUM_ARTIST("album_artist"),
  ARTIST("artist"),
  DATE("date"),
  TITLE("title"),
  EXTENSION("extension"),
  DISC("disc"),
  TRACK("track");

  @Getter private final String key;

  public static MusicTags parseFrom(@NonNull String s) throws BadMusicTagException {
    return Arrays.stream(values())
        .filter(musicTags -> musicTags.key.equals(s.toLowerCase(Locale.ROOT)))
        .findFirst()
        .orElseThrow(() -> new BadMusicTagException(s));
  }

  /**
   * Tags which are widespread enough to be recovered from any format. Other tags might not be read
   * from WAV and MP3 files, specifically.
   */
  @Getter
  private static final Set<MusicTags> ubiquitousTags =
      Set.of(ALBUM, ARTIST, DATE, TITLE, EXTENSION, TRACK);
}
