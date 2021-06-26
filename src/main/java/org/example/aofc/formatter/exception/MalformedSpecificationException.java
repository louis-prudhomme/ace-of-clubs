package org.example.aofc.formatter.exception;

public class MalformedSpecificationException extends SpecificationParsingException {
  public MalformedSpecificationException(String spec) {
    super(String.format("The specification is malformed %s", spec));
  }
}
