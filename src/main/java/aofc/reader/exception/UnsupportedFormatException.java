package aofc.reader.exception;

import lombok.NonNull;

public class UnsupportedFormatException extends MusicFileException {
  public UnsupportedFormatException(@NonNull String format) {
    super(
        String.format(
            "The format «%s» is recognized as a music format, but is not supported at the moment",
            format));
  }
}
