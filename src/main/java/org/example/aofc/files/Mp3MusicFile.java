package org.example.aofc.files;

import com.mpatric.mp3agic.Mp3File;
import lombok.Data;
import lombok.NonNull;
import org.example.aofc.MusicTags;
import org.example.aofc.files.exception.MusicFileException;

import java.util.Optional;

@Data
public class Mp3MusicFile implements IMusicFile {
  private final String path;
  private final Mp3File audioFile;

  public Mp3MusicFile(@NonNull String path) {
    this.path = path;
    try {
      this.audioFile = new Mp3File(path);
    } catch (Exception e) {
      throw new MusicFileException(e);
    }
  }

  @Override
  public @NonNull Optional<String> getTag(@NonNull MusicTags key) {
    return getId3v2Tag(key).isPresent() ? getId3v2Tag(key) : getId3v1Tag(key);
  }

  private @NonNull Optional<String> getId3v2Tag(@NonNull MusicTags key) {
    return Optional.ofNullable(switch (key) {
      case ALBUM -> audioFile.getId3v2Tag().getAlbum();
      case ALBUM_ARTIST -> audioFile.getId3v2Tag().getAlbumArtist();
      case ARTIST -> audioFile.getId3v2Tag().getArtist();
      case DATE -> audioFile.getId3v2Tag().getDate();
      case TITLE -> audioFile.getId3v2Tag().getTitle();
    });
  }

  private @NonNull Optional<String> getId3v1Tag(@NonNull MusicTags key) {
    return Optional.ofNullable(switch (key) {
      case ALBUM -> audioFile.getId3v1Tag().getAlbum();
      case ALBUM_ARTIST, ARTIST -> audioFile.getId3v1Tag().getArtist();
      case DATE -> audioFile.getId3v1Tag().getYear();
      case TITLE -> audioFile.getId3v1Tag().getTitle();
    });
  }
}
