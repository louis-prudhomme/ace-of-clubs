import ch.qos.logback.classic.encoder.PatternLayoutEncoder

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%-4level @ %thread] %logger{4} - %msg%n"
    }
}

logger("ws.schild", OFF)

root(INFO, ["STDOUT"])