package org.example.aofc.writer;

import lombok.Data;
import lombok.NonNull;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.utils.FileUtils;

@Data
public class Transponder {
  private final MusicFileFactory factory = new MusicFileFactory();
  private final String path;

  public @NonNull String getOfficialRelativePath() {
    return assembleOfficialRelativePath(factory.make(path));
  }

  private @NonNull String assembleOfficialRelativePath(@NonNull IMusicFile file) {
    return String.format(
        "%s/%s – %s/%s.%s",
        file.getAlbumArtistTag().orElseThrow(),
        file.getDateTag().orElseThrow(),
        file.getAlbumTag().orElseThrow(),
        file.getTitleTag().orElseThrow(),
        FileUtils.getExtension(path).orElseThrow());
  }
}
