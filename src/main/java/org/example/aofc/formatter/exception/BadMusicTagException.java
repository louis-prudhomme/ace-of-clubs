package org.example.aofc.formatter.exception;

public class BadMusicTagException extends SpecificationParsingException {
  public BadMusicTagException(String badTag) {
    super(badTag);
  }
}