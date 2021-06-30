package aofc.formatter.exception;

public class UnevenSpecificationException extends SpecificationParsingException {
  public UnevenSpecificationException(long nbLeft, long nbRight) {
    super(String.format("Uneven marker number found : %d × [ & %d × ].", nbLeft, nbRight));
  }
}
