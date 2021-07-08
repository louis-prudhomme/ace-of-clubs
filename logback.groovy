import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.DEBUG

appender("STDOUT", ConsoleAppender) {
    target = "System.out"
    encoder(PatternLayoutEncoder) {
        pattern = "%d{mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
}

root(DEBUG, ["STDOUT"])