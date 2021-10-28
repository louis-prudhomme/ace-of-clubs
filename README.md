# Ace of Clubs

## What is this ?

As sources for music files can differ (Bandcamp, iTunes and other legal(ish(?))) sources), and their respective file
organization greatly vary, you might suffer from your filesystem being so cluttered.

Moreover, as automatic tagging exists (notably through solutions such as Discogs or MusicBrainz, in conjunction with
MediaMonkey or Mp3tag), why bother sorting your own files yourself ? Worse even, if you tend to your tag manually, why
inflicting yourself this burden twice ?

This project is a music file organizer. It aims to help you sort your musics files using their tags. When your music
library is tagged according to your will, you just have to run a command-line and specify the original path, and the
destination path ; your files will be sorted and unless you are trying to compete with Spotify’s library, it will likely
take just enough time to brew (maybe drink) a coffee.

## Okay, how does this work ?

The runnable is pretty straightforward. Put it in your PATH (or invoke it from the folder where it is located). At least
two arguments must be provided :

- origin path, where the program will find your music files
- destination path, where the program will put your files once sorted.

More options are available to specify whether you want to move or copy our files, or what the program should do if the
sorted file already exists. The timeout can also be configured.

Finally, you can customize how the program sorts your file ; the default mask
is `[album_artist]/[date] – [album]/[disc]-[track] – [title].[extension]`, but you can ditch the tags you don't need or
change the order as well as the characters. More customization options will come in time! :)

Optionally, a functionality is available to transcode WAV and MP3 files to FLAC. It must be
activated with a specific flag.

## But technically, how does this work ?

This is a Java 16, Maven-powered project, which should run on major operating systems (or those that support Java at the
very least).

The program makes leverages the Java 9 Flow API to parallelize the different storing steps (finding, naming and moving
so far).

Technically, the program operates through a stream of different steps ;

1. Finding the music files at the specified location
2. Optionally, transcoding the file to another format (FLAC or Opus)
3. Computing the new path from the file tags
4. Writing (move or copy) the sorted music files

## How to package the project ?

The project is packaged through Maven. For the moment, only a JAR is available (and you can download it on the main
page).

Alternatively, you can download the project files and package them with Maven using `mvn clean install`.

## What is planned in the future ?

A substantially long list of improvements and features already popped while I was using my own solution :

### Improvements

- [ ] Add tests
- [ ] Package the program (GraalVM ?)
- [x] Fix logger formatting (too ugly)
    - [x] Fix broken logger
- [ ] Improve program safety 
  - [ ] ...by correctly handling exceptions everywhere
  - [ ] ...by adding top-level exception handlers

### Features

- [ ] Add a new step in the stream to convert music files (notably to FLAC or Opus, which are more easily tagged)
    - [x] Flac
    - [ ] Opus
- [ ] Add a «rejected» folder
- [ ] Formatting engine
  - [x] Extract simple tags
  - [ ] Extract multivalued tags (with the `!` operator)
  - [ ] Truncate tags (with the `{x}:<tag>:{y}` operator)
  - [ ] Pad tags (with the `<tag>{<char>,<x>}`)
  - [ ] Add customization for fallback characters (instead of `_`)
- [ ] Add automatic tagging (through an online service)