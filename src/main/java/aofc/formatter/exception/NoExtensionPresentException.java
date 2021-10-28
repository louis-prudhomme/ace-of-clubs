package aofc.formatter.exception;

import aofc.reader.exception.MusicFileException;

public class NoExtensionPresentException extends MusicFileException {
  public NoExtensionPresentException(String cause) {
    super(String.format("%s does not have any extension", cause));
  }
}
