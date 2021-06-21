package org.example.aofc.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerConfig {
  public static void ConfigureLogger() {
    var pin = new Logger[] {Logger.getLogger("org.jaudiotagger")};
    for (Logger l : pin) l.setLevel(Level.OFF);
  }
}
