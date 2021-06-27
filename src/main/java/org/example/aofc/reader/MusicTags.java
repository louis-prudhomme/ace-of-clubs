package org.example.aofc.reader;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.aofc.formatter.exception.BadMusicTagException;

import java.util.Arrays;
import java.util.Locale;

@RequiredArgsConstructor
public enum MusicTags {
  ALBUM("album"),
  ALBUM_ARTIST("albart"),
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
}
