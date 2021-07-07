package aofc.formatter.exception;

public abstract class SpecificationFormattingException extends Exception {
  public SpecificationFormattingException(String message) {
    super(message);
  }

  public SpecificationFormattingException(String message, Throwable cause) {
    super(message, cause);
  }

  public SpecificationFormattingException(Throwable cause) {
    super(cause);
  }
}
