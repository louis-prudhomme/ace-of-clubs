package aofc.formatter.provider.exception;

public class CouldNotParseDateException extends TagProviderException {
  public CouldNotParseDateException(String message, Throwable cause) {
    super(String.format("Could not parse date « %s »", message), cause);
  }
}
