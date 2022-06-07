package aofc.reader;

import aofc.reader.exception.MusicFileException;
import aofc.utils.FileUtils;
import com.mpatric.mp3agic.*;
import lombok.Data;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Data
public class Mp3MusicFile implements MusicFile {
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private final Path path;
  private final Mp3File audioFile;
  private final ID3Wrapper wrapper;

  public Mp3MusicFile(@NonNull Path path) {
    this.path = path;
    try {
      this.audioFile = new Mp3File(path);
    } catch (IOException | UnsupportedTagException | InvalidDataException e) {
      throw new MusicFileException(e);
    }
    this.wrapper = new ID3Wrapper(audioFile.getId3v1Tag(), audioFile.getId3v2Tag());

    if (audioFile.hasCustomTag()) {
      var encoded = new EncodedText(audioFile.getCustomTag());
      logger.info(encoded.toString());
    }
  }

  @Override
  public @NonNull Optional<String> getTag(@NonNull MusicTags key) {
    return switch (key) {
      case DISC -> Optional.ofNullable(wrapper.getId3v2Tag().getPartOfSet());
      case COMPOSER -> Optional.ofNullable(wrapper.getComposer());
      case PUBLISHER -> Optional.ofNullable(wrapper.getId3v2Tag().getPublisher());
      case GENRE -> Optional.ofNullable(wrapper.getGenreDescription());
      case ALBUM -> Optional.ofNullable(wrapper.getAlbum());
      case ALBUM_ARTIST -> Optional.ofNullable(wrapper.getAlbumArtist());
      case ARTIST -> Optional.ofNullable(wrapper.getArtist());
      case DATE -> Optional.ofNullable(wrapper.getYear());
      case TITLE -> Optional.ofNullable(wrapper.getTitle());
      case TRACK -> Optional.ofNullable(wrapper.getTrack());
      case EXTENSION -> FileUtils.getExtension(path);
    };
  }
}
