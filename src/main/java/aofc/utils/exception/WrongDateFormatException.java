package aofc.utils.exception;

public class WrongDateFormatException extends Exception {
  public WrongDateFormatException(String date) {
    super(String.format("The date « %s » could not be parsed.", date));
  }
}
