import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.DEBUG

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%-4level @ %thread] %logger{4} - %msg%n"
    }
}

logger("ws.schild", OFF)
logger("org.jaudiotagger", OFF)
logger("org.jaudiotagger.audio", OFF)
logger("org.jaudiotagger.tag", OFF)
root(DEBUG, ["STDOUT"])