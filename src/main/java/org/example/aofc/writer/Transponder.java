package org.example.aofc.writer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.example.aofc.reader.IMusicFile;
import org.example.aofc.reader.MusicFileFactory;
import org.example.aofc.utils.DateUtils;
import org.example.aofc.utils.FileUtils;

import java.util.concurrent.Flow;

@Data
@EqualsAndHashCode(callSuper = false)
public class Transponder implements Flow.Subscriber<String> {
  private static final int TRACK_PAD_SIZE = 3;
  private static final String TRACK_PAD_CHAR = "0";
  private final MusicFileFactory factory = new MusicFileFactory();
  private Flow.Subscription subscription;

  private @NonNull String getRelativePath(@NonNull IMusicFile file, @NonNull String path) {
    var sanitizer =
        new Sanitizer(
            file.getAlbumArtistTag(),
            file.getDateTag().map(DateUtils::getDate),
            file.getAlbumTag(),
            file.getTrackTag().map(s -> StringUtils.leftPad(s, TRACK_PAD_SIZE, TRACK_PAD_CHAR)),
            file.getTitleTag(),
            FileUtils.getExtension(path));

    return String.format(
        "%s/%s – %s/%s – %s.%s",
        (Object[]) sanitizer.santizeBulk()); // todo allow custom formatting in command line option
    // todo refactor fkin ugly ass cast
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
