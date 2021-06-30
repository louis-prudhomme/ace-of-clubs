package aofc.formatter.exception;

public class BadMusicTagException extends SpecificationParsingException {
  public BadMusicTagException(String badTag) {
    super(String.format("The tag « %s » is incorrect", badTag));
  }
}
