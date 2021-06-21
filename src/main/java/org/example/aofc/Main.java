package org.example.aofc;

import org.example.aofc.files.MusicFileFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    var pin = new Logger[] {Logger.getLogger("org.jaudiotagger")};
    for (Logger l : pin) l.setLevel(Level.OFF);
    var mff = new MusicFileFactory();

    var l =
        List.of(
                "D:/hella/Downloads/To Victory.flac",
                "D:/hella/Downloads/03 Je suis d'ailleurs.mp3")
            .stream()
            .map(mff::make)
            .collect(Collectors.toList());

    for (var f : l) {
      System.out.println(f.getTag(MusicTags.ALBUM).orElse(null));
      System.out.println(f.getTag(MusicTags.ALBUM_ARTIST).orElse(null));
      System.out.println(f.getTag(MusicTags.ARTIST).orElse(null));
      System.out.println(f.getTag(MusicTags.DATE).orElse(null));
      System.out.println(f.getTag(MusicTags.TITLE).orElse(null));
      System.out.println("==============================================");
    }
  }
}
