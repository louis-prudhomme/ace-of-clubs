package org.example.aofc.reader;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import lombok.Data;
import lombok.NonNull;
import org.example.aofc.reader.exception.MusicFileException;
import org.example.aofc.utils.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Data
public class Mp3MusicFile implements MusicFile {
  private final Path path;
  private final Mp3File audioFile;

  public Mp3MusicFile(@NonNull Path path) {
    this.path = path;
    try {
      this.audioFile = new Mp3File(path);
    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
      throw new MusicFileException(e);
    }
  }

  @Override
  public @NonNull Optional<String> getTag(@NonNull MusicTags key) {
    var tentative = Optional.ofNullable(getId3v2Tag(key));
    return tentative.isPresent() ? tentative : Optional.ofNullable(getId3v1Tag(key));
  }

  private @Nullable String getId3v2Tag(@NonNull MusicTags key) {
    return switch (key) {
      case ALBUM -> audioFile.getId3v2Tag().getAlbum();
      case ALBUM_ARTIST -> audioFile.getId3v2Tag().getAlbumArtist();
      case ARTIST -> audioFile.getId3v2Tag().getArtist();
      case DATE -> audioFile.getId3v2Tag().getDate();
      case TITLE -> audioFile.getId3v2Tag().getTitle();
      case EXTENSION -> FileUtils.getExtension(path).orElse(null);
      case TRACK -> audioFile.getId3v2Tag().getTrack();
      case DISC -> null;
    };
  }

  private @Nullable String getId3v1Tag(@NonNull MusicTags key) {
    return switch (key) {
      case ALBUM -> audioFile.getId3v1Tag().getAlbum();
      case ALBUM_ARTIST, ARTIST -> audioFile.getId3v1Tag().getArtist();
      case DATE -> audioFile.getId3v1Tag().getYear();
      case TITLE -> audioFile.getId3v1Tag().getTitle();
      case EXTENSION -> FileUtils.getExtension(path).orElse(null);
      case TRACK -> audioFile.getId3v1Tag().getTrack();
      case DISC -> null;
    };
  }
}
