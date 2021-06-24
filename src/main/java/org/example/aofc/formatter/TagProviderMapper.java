package org.example.aofc.formatter;

import lombok.NonNull;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicTags;

import java.util.Optional;
import java.util.function.Function;

public class TagProviderMapper {
  public static Function<IMusicFile, Optional<String>> getProviderFor(@NonNull MusicTags tag) {
    return iMusicFile -> iMusicFile.getTag(tag);
  }
}
