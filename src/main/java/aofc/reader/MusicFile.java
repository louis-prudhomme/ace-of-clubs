package aofc.reader;

import lombok.NonNull;

import java.nio.file.Path;
import java.util.Optional;

public interface MusicFile {
  @NonNull
  Path getPath();

  @NonNull
  Optional<String> getTag(@NonNull MusicTags key);

  @NonNull
  default Optional<String> getAlbumTag() {
    return getTag(MusicTags.ALBUM);
  }

  @NonNull
  default Optional<String> getAlbumArtistTag() {
    return getTag(MusicTags.ALBUM_ARTIST);
  }

  @NonNull
  default Optional<String> getArtistTag() {
    return getTag(MusicTags.ARTIST);
  }

  @NonNull
  default Optional<String> getTrackTag() {
    return getTag(MusicTags.TRACK);
  }

  @NonNull
  default Optional<String> getDiscTag() {
    return getTag(MusicTags.DISC);
  }

  @NonNull
  default Optional<String> getDateTag() {
    return getTag(MusicTags.DATE);
  }

  @NonNull
  default Optional<String> getTitleTag() {
    return getTag(MusicTags.TITLE);
  }
}
