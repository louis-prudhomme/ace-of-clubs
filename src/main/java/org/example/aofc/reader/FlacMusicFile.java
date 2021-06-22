package org.example.aofc.reader;

import lombok.Data;
import lombok.NonNull;
import org.example.aofc.reader.exception.MusicFileException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagField;

import java.io.File;
import java.util.Optional;

@Data
public class FlacMusicFile implements IMusicFile {
  private final String path;
  private final AudioFile audioFile;

  public FlacMusicFile(@NonNull String path) {
    try {
    this.path = path;
    this.audioFile = AudioFileIO.read(new File(path));
    } catch (Exception e) { throw new MusicFileException(e);}
  }

  @Override
  public @NonNull Optional<String> getTag(@NonNull MusicTags key) {
    return Optional.ofNullable(switch (key) {
      case ALBUM -> audioFile.getTag().getFields(FieldKey.ALBUM);
      case ALBUM_ARTIST -> audioFile.getTag().getFields(FieldKey.ALBUM_ARTIST);
      case ARTIST -> audioFile.getTag().getFields(FieldKey.ARTIST);
      case DATE -> audioFile.getTag().getFields(FieldKey.YEAR);
      case TITLE -> audioFile.getTag().getFields(FieldKey.TITLE);
      case TRACK -> audioFile.getTag().getFields(FieldKey.TRACK);
    }).map(tagFields -> tagFields.get(0)).map(TagField::toString);
  }
}
