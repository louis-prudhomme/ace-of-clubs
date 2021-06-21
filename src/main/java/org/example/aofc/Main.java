package org.example.aofc;

import org.example.aofc.utils.LoggerConfig;
import org.example.aofc.writer.Transponder;

import java.util.List;

public class Main {
  public static void main(String[] args) {
    LoggerConfig.ConfigureLogger();

    List.of("D:/hella/Downloads/To Victory.flac", "D:/hella/Downloads/03 Je suis d'ailleurs.mp3")
        .stream()
        .map(Transponder::new)
        .map(Transponder::getOfficialRelativePath)
        .forEach(System.out::println);
  }
}
