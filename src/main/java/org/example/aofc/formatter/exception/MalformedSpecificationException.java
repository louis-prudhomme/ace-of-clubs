package org.example.aofc.formatter.exception;

public class MalformedSpecificationException extends SpecificationParsingException {
  public MalformedSpecificationException(String spec) {
    super(spec);
  }
}
