package aofc.formatter.provider.exception;

import aofc.formatter.exception.SpecificationFormattingException;

public abstract class TagProviderException extends SpecificationFormattingException {
  public TagProviderException(String message) {
    super(message);
  }

  public TagProviderException(String message, Throwable cause) {
    super(message, cause);
  }

  public TagProviderException(Throwable cause) {
    super(cause);
  }
}
