package aofc.command;

import aofc.command.conversion.*;
import aofc.fluxer.FluxFactory;
import aofc.fluxer.Fluxer;
import aofc.formatter.SpecificationFormatter;
import aofc.transcoder.Codec;
import aofc.transcoder.Transcoder;
import aofc.transcoder.TranscodingModes;
import aofc.transponder.Transponder;
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

import static aofc.transcoder.TranscodingModes.NO_TRANSCODING;

@Command(
    name = "aofc",
    mixinStandardHelpOptions = true,
    description = "Music file sorter.",
    version = "0.5",
    exitCodeListHeading = "Exit codes:\n",
    exitCodeList = {
      "0\t:\tSuccessful program execution.",
      "2\t:\tArg parsing error.",
      "1000\t:\tProgram timed out."
      // ,"1500\t:\tProgram was interrupted." todo
    })
public class AofC implements Callable<Integer> {
  private final Logger logger = LoggerFactory.getLogger("aofc");

  private static final java.util.logging.Logger PIN_THAT_LOGGER;

  static {
    PIN_THAT_LOGGER = java.util.logging.Logger.getLogger("org.jaudiotagger");
    PIN_THAT_LOGGER.setLevel(Level.OFF);
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
          "Format of the file names, including any subfolder. Music file tags and data can be specified between [square brackets]. The default value is ?? ${DEFAULT-VALUE} ??",
      defaultValue = "[album_artist]/[date] ??? [album]/[disc]-[track] ??? [title].[extension]")
  private String specificationArg;

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
          "What should the program do when a music file already exists. Must be one of ?? ${COMPLETION-CANDIDATES} ??. Default is ?? ${DEFAULT-VALUE} ??.",
      converter = FileExistsModeArgConverter.class,
      completionCandidates = FileExistsMode.Enumeration.class,
      defaultValue = "replace")
  private FileExistsMode fileExistsMode;

  @Option(
      names = {"-t", "--timeout"},
      description =
          "How much time should the program run before stopping. Default is ?? ${DEFAULT-VALUE} ??.",
      defaultValue = "30")
  private long timeout = 30;

  @Option(
      names = {"-ci", "--ignore-good"},
      description =
          "Should the program ignore \"good encodings\". Default is ?? ${DEFAULT-VALUE} ??.",
      defaultValue = "0")
  private int ignoreGoodEncodings = 0;

  @Option(
      names = {"-mm", "--move-mode"},
      description =
          "Whether the program should copy or move the files. Must be one of ?? ${COMPLETION-CANDIDATES} ??. Default is ?? ${DEFAULT-VALUE} ??.",
      converter = MoveModeArgConverter.class,
      completionCandidates = MoveMode.Enumeration.class,
      defaultValue = "move")
  private MoveMode moveMode;

  @Option(
      names = {"-cc", "--codec"},
      description =
          "What codec should the program transcode non-supported files to. Must be one of ?? ${COMPLETION-CANDIDATES} ??. Default is ?? ${DEFAULT-VALUE} ??.",
      converter = EncodingCodecArgConverter.class,
      completionCandidates = Codec.Enumeration.class,
      defaultValue = "flac")
  private Codec codec = Codec.FLAC;

  @Option(
      names = {"-tc", "--transcoding"},
      description =
          "Whether the program should transcode unsupported files. WARNING setting this option can greatly slow the program execution. Must be one of ?? ${COMPLETION-CANDIDATES} ??. Default is ?? ${DEFAULT-VALUE} ??.",
      converter = TranscodingModeConverter.class,
      completionCandidates = TranscodingModes.Enumeration.class,
      defaultValue = "0")
  private TranscodingModes transcoding = NO_TRANSCODING;

  @Override
  public Integer call() {
    var originPath = FileUtils.checkPath(originPathArg);
    var destinationPath = FileUtils.checkPath(destinationPathArg, CheckPathMode.OSEF);
    var specification = new SpecificationFormatter(specificationArg, replacer);

    logger.info("Origin ?? {} ??", originPath);
    logger.info("Destination ?? {} ??", destinationPath);
    logger.info("Specification ?? {} ??", specificationArg);
    logger.info("FileExistsMode ?? {} ??", fileExistsMode.toString());
    logger.info("MoveMode ?? {} ??", moveMode.toString());
    logger.info("Transcoding mode ?? {} ??", transcoding);
    logger.info("Codec ?? {} ??", codec.toString());
    if (transcoding != NO_TRANSCODING)
      logger.warn(String.format("Transcoding activated (%s)", transcoding));
    if (timeout == 0) {
      timeout = Long.MAX_VALUE - 1;
      logger.warn("No timeout");
    } else logger.info("Timeout ?? {} ??", timeout);

    var transcoder =
        transcoding != NO_TRANSCODING ? new Transcoder(ignoreGoodEncodings > 0, codec) : null;
    var transponder = new Transponder(specification, destinationPath);
    var mover = new Mover(fileExistsMode, moveMode);

    var factory = new FluxFactory(transcoder, transponder);
    var fluxer = new Fluxer(factory::getInstance, mover, originPath);

    return fluxer.handle(timeout);
  }

  public static void main(@NonNull String[] args) {
    System.exit(
        new CommandLine(new AofC())
            .setExecutionExceptionHandler(new CommandExceptionHandler())
            .execute(args));
  }
}
