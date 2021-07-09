package aofc.command;

import aofc.command.conversion.*;
import aofc.formatter.SpecificationFormatter;
import aofc.scrapper.FlaggerPublisher;
import aofc.transcoder.TranscoderProcessor;
import aofc.transponder.EncodingCodecs;
import aofc.transponder.TransponderProcessor;
import aofc.utils.CheckPathMode;
import aofc.utils.FileUtils;
import aofc.writer.FileExistsMode;
import aofc.writer.MoveMode;
import aofc.writer.Mover;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.util.logging.Level;

@Command(
    name = "aofc",
    mixinStandardHelpOptions = true,
    description = "Music file sorter.",
    version = "0.5",
    exitCodeListHeading = "Exit codes:\n",
    exitCodeList = {
      "0\t:\tSuccessful program execution.",
      "2\t:\tArg parsing error.",
      "1000\t:\tProgram timed out.",
      "1500\t:\tProgram was interrupted."
    })
public class AofC implements Callable<Integer> {
  private final Logger logger = LoggerFactory.getLogger("aofc");
  static {
    java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(Level.OFF);
  }

  @Parameters(
      index = "0",
      defaultValue = ".",
      description = "Path of the folder to index and sort.")
  private String originPathArg;

  @Parameters(
      index = "1",
      defaultValue = "./Sorted/",
      description = "Path of the folder where to move the music files.")
  private String destinationPathArg;

  @Option(
      names = {"-f", "--format"},
      description =
          "Format of the file names, including any subfolder. Music file tags and data can be specified between [square brackets]. The default value is « ${DEFAULT-VALUE} »",
      defaultValue = "[album_artist]/[date] – [album]/[disc]-[track] – [title].[extension]")
  private String specificationArg;

  @Option(
      names = {"-t", "--timeout"},
      description =
          "How much time do you want to wait for completion (in seconds). If zero, no timeout will be expected (the program will complete when all files are sorted). Default: ${DEFAULT-VALUE}",
      defaultValue = "10")
  private Integer timeout = 10;

  @Option(
      names = {"-rc", "--replacement-char"},
      description =
          "String by which characters otherwise forbidden in a path will be replaced. Must not be a forbidden character. Default: ${DEFAULT-VALUE}",
      converter = ReplacerCharacterValidator.class,
      defaultValue = "_")
  private String replacer = "_";

  @Option(
      names = {"-fm", "--file-exist-mode"},
      description =
          "What should the program do when a music file already exists. Must be one of « ${COMPLETION-CANDIDATES} ». Default is « ${DEFAULT-VALUE} ».",
      converter = FileExistsModeArgConverter.class,
      completionCandidates = FileExistsMode.Enumeration.class,
      defaultValue = "replace")
  private FileExistsMode fileExistsMode;

  @Option(
      names = {"-mm", "--move-mode"},
      description =
          "Whether the program should copy or move the files. Must be one of « ${COMPLETION-CANDIDATES} ». Default is « ${DEFAULT-VALUE} ».",
      converter = MoveModeArgConverter.class,
      completionCandidates = MoveMode.Enumeration.class,
      defaultValue = "move")
  private MoveMode moveMode;

  @Option(
      names = {"-cc", "--codec"},
      description =
          "What codec should the program transcode non-supported files to. Must be one of « ${COMPLETION-CANDIDATES} ». Default is « ${DEFAULT-VALUE} ».",
      converter = EncodingCodecArgConverter.class,
      completionCandidates = EncodingCodecs.Enumeration.class,
      defaultValue = "flac")
  private EncodingCodecs codec = EncodingCodecs.FLAC;

  @Option(
      names = {"-tc", "--transcoding"},
      description =
          "Whether the program should transcode unsupported files. 0 for no transcoding, 1 for WAV transcoding, 2 for WAV and MP3. WARNING setting this option can greatly slow the program execution. Default is « ${DEFAULT-VALUE} ».",
      converter = TranscodingModeCharacterValidator.class,
      defaultValue = "0")
  private int transcodingMode = 0;

  @Override
  public Integer call() {
    var originPath = FileUtils.checkPath(this.originPathArg);
    var destinationPath = FileUtils.checkPath(this.destinationPathArg, CheckPathMode.OSEF);
    var specification = new SpecificationFormatter(specificationArg, replacer);

    logger.debug("Origin « {} »", originPath.toString());
    logger.debug("Destination « {} »", destinationPath.toString());
    logger.debug("Specification « {} »", specificationArg);
    logger.debug("Timeout {} seconds", timeout);
    logger.debug("FileExistsMode « {} »", fileExistsMode.toString());
    logger.debug("MoveMode « {} »", moveMode.toString());
    logger.debug("Codec mode « {} »", switch (transcodingMode) {
        case 0 -> "No transcoding";
        case 1 -> "WAV only";
        case 2 -> "WAV & MP3";
        default -> throw new RuntimeException("not possible");});
    logger.debug("Codec « {} »", codec.toString());
    if (transcodingMode != 0) logger.warn("Transcoding activated");

    var scrapper = new FlaggerPublisher(originPath);
    var transcoder = new TranscoderProcessor(EncodingCodecs.FLAC);
    var transponder = new TransponderProcessor(specification, destinationPath);
    var mover = new Mover(fileExistsMode, moveMode);

    if (timeout <= 0) timeout = Integer.MAX_VALUE;

    try {
      if (transcodingMode == 0) {
        scrapper.submit(transponder);
        transponder.submit(mover);
        return scrapper.await(timeout) && transponder.await(timeout) ? 0 : 1000;
      } else {
        scrapper.submit(transcoder);
        transcoder.submit(transponder);
        transponder.submit(mover);
        return scrapper.await(timeout) && transcoder.await(timeout) && transponder.await(timeout)
            ? 0
            : 1000;
      }
    } catch (InterruptedException e) {
      return 1500;
    }
  }

  public static void main(@NonNull String[] args) {
    System.exit(
        new CommandLine(new AofC())
            .setExecutionExceptionHandler(new ExceptionHandler())
            .execute(args));
  }
}
