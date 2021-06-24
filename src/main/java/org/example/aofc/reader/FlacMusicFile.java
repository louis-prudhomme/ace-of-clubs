package org.example.aofc.reader;

import lombok.Data;
import lombok.NonNull;
import org.example.aofc.reader.exception.MusicFileException;
import org.example.aofc.utils.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@Data
public class FlacMusicFile implements IMusicFile {
  private final Path path;
  private final AudioFile audioFile;

  public FlacMusicFile(@NonNull Path path) {
    try {
    this.path = path;
    this.audioFile = AudioFileIO.read(path.toFile());
    } catch (IOException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) { throw new MusicFileException(e);}
  }

  @Override
  public @NonNull Optional<String> getTag(@NonNull MusicTags key) {
    return Optional.ofNullable(switch (key) {
      case ALBUM -> extractTag(audioFile, FieldKey.ALBUM);
      case ALBUM_ARTIST -> extractTag(audioFile,FieldKey.ALBUM_ARTIST);
      case ARTIST -> extractTag(audioFile,FieldKey.ARTIST);
      case DATE -> extractTag(audioFile,FieldKey.YEAR);
      case TITLE -> extractTag(audioFile,FieldKey.TITLE);
      case EXTENSION -> FileUtils.getExtension(path).orElse(null);
      case TRACK -> extractTag(audioFile,FieldKey.TRACK);
    });
  }

  private @NonNull String extractTag(@NonNull AudioFile tag, @NonNull FieldKey key) {
    return tag.getTag().getFields(key).get(0).toString();
  }
}
