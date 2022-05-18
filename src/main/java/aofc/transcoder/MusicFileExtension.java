package aofc.transcoder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public enum MusicFileExtension {
    FLAC("flac"),
    OPUS("ogg");

    @Getter private final String arg;

    public static MusicFileExtension FromEncodingCodec(EncodingCodecs codec) {
        return switch (codec) {
            case FLAC -> FLAC;
            case OPUS -> OPUS;
        };
    }
}
