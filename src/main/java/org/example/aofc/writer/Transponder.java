package org.example.aofc.writer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.utils.DateUtils;
import org.example.aofc.utils.FileUtils;

import java.util.concurrent.Flow;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transponder implements Flow.Subscriber<String> {
  private final MusicFileFactory factory = new MusicFileFactory();
  private Flow.Subscription subscription;

  private @NonNull String getRelativePath(@NonNull IMusicFile file, @NonNull String path) {
    return String.format(
        "%s/%s – %s/%03d – %s.%s",
        file.getAlbumArtistTag().orElseThrow(),
        DateUtils.getDate(file.getDateTag().orElseThrow()).orElseThrow(),
        file.getAlbumTag().orElseThrow(),
        file.getTrackTag().map(Integer::parseInt).orElseThrow(),
        file.getTitleTag().orElseThrow(),
        FileUtils.getExtension(path).orElseThrow());
  }

  @Override
  public void onSubscribe(@NonNull Flow.Subscription subscription) {
    (this.subscription = subscription).request(Long.MAX_VALUE);
  }

  @Override
  public void onNext(@NonNull String path) {
    System.out.println(getRelativePath(factory.make(path), path));
  }

  @Override
  public void onError(@NonNull Throwable throwable) {
    throw new RuntimeException(throwable);
  }

  @Override
  public void onComplete() {}
}
