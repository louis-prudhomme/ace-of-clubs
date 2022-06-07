package aofc.reader;

import aofc.reader.exception.MusicFileException;
import aofc.utils.FileUtils;
import lombok.Data;
import lombok.NonNull;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Data
public class FlacMusicFile implements MusicFile {
  private final Path path;
  private final AudioFile audioFile;

  public FlacMusicFile(@NonNull Path path) {
    try {
    this.path = path;
    this.audioFile = AudioFileIO.read(path.toFile());
    } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
      throw new MusicFileException(e);
    }
  }

  @Override
  public @NonNull Optional<String> getTag(@NonNull MusicTags key) {
    return Optional.ofNullable(switch (key) {
      case ALBUM -> extractTag(audioFile, FieldKey.ALBUM);
      case ALBUM_ARTIST -> extractTag(audioFile, FieldKey.ALBUM_ARTIST);
      case ARTIST -> extractTag(audioFile, FieldKey.ARTIST);
      case DATE -> extractTag(audioFile, FieldKey.YEAR);
      case TITLE -> extractTag(audioFile, FieldKey.TITLE);
      case EXTENSION -> FileUtils.getExtension(path).orElse(null);
      case COMPOSER -> extractTag(audioFile, FieldKey.COMPOSER);
      case PUBLISHER -> extractTag(audioFile, FieldKey.PRODUCER);
      case GENRE -> extractTag(audioFile, FieldKey.GENRE);
      case TRACK -> extractTag(audioFile, FieldKey.TRACK);
      case DISC -> extractTag(audioFile, FieldKey.DISC_NO);
    });
  }

  private @Nullable String extractTag(@NonNull AudioFile tag, @NonNull FieldKey key) {
    try {
      // todo handle multivalued fields
      return tag.getTag().getFields(key).stream()
              .map(TagField::toString)
              .reduce((tagField, tagField2) -> String.join(";", tagField, tagField2))
              .orElse(null);
    } catch (IndexOutOfBoundsException e) {
      // todo log
      return null;
    }
  }
}
