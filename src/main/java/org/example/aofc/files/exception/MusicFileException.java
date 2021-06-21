package org.example.aofc.files.exception;

public class MusicFileException extends RuntimeException {
  public MusicFileException() {
    super();
  }

  public MusicFileException(String message) {
    super(message);
  }

  public MusicFileException(String message, Throwable cause) {
    super(message, cause);
  }

  public MusicFileException(Throwable cause) {
    super(cause);
  }

  protected MusicFileException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
