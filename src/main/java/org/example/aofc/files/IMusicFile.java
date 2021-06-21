package org.example.aofc.files;

import lombok.NonNull;
import org.example.aofc.MusicTags;

import java.util.Optional;

public interface IMusicFile {

  @NonNull
  Optional<String> getTag(@NonNull MusicTags key);
}
