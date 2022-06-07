package aofc.formatter;

import aofc.formatter.exception.BadMusicTagException;
import aofc.reader.MusicTags;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public enum MusicFileVorbisTags {
    ALBUM("ALBUM"),
    ALBUM_ARTIST("ALBUMARTIST"),
    ARTIST("ARTIST"),
    DATE("YEAR"),
    TITLE("TITLE"),
    COMPOSER("COMPOSER"),
    PUBLISHER("PUBLISHER"),
    DISC("DISCNUMBER"),
    GENRE("GENRE"),
    TRACK("TRACK");

    @Getter private final String key;

    public static Optional<MusicFileVorbisTags> convert(@NonNull MusicTags t) throws BadMusicTagException {
        return Optional.ofNullable(switch (t) {
            case ALBUM -> ALBUM;
            case ALBUM_ARTIST -> ALBUM_ARTIST;
            case ARTIST -> ARTIST;
            case DATE -> DATE;
            case TITLE -> TITLE;
            case EXTENSION -> null;
            case DISC -> DISC;
            case COMPOSER -> COMPOSER;
            case PUBLISHER -> PUBLISHER;
            case GENRE -> GENRE;
            case TRACK -> TRACK;
        });
    }

    /**
     * Tags which are widespread enough to be recovered from any format. Other tags might not be read
     * from WAV and MP3 files, specifically.
     */
    @Getter
    private static final Set<MusicFileVorbisTags> ubiquitousTags =
            Set.of(ALBUM, DATE, TRACK, TITLE, DISC, ALBUM_ARTIST, COMPOSER, PUBLISHER, GENRE, ARTIST);
}
