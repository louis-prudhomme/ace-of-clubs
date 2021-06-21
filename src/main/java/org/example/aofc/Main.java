package org.example.aofc;

import org.example.aofc.writer.Transponder;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
  public static void main(String[] args) {
    var pin = new Logger[] {Logger.getLogger("org.jaudiotagger")};
    for (Logger l : pin) l.setLevel(Level.OFF);

    List.of("D:/hella/Downloads/To Victory.flac", "D:/hella/Downloads/03 Je suis d'ailleurs.mp3")
        .stream()
        .map(Transponder::new)
        .map(Transponder::getOfficialRelativePath)
        .forEach(System.out::println);
  }
}
