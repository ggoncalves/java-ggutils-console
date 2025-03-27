package com.ggoncalves.ggutils.console.cli;

import com.ggoncalves.ggutils.console.exception.FilePermissionException;
import com.ggoncalves.ggutils.console.exception.InvalidFileException;
import com.ggoncalves.ggutils.console.validation.FilePathValidator;
import com.ggoncalves.ggutils.console.validation.ValidationResult;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;

public class CommandProcessor {
  private final FilePathValidator filePathValidator;
  private final Options options;

  public CommandProcessor(FilePathValidator filePathValidator) {
    this.filePathValidator = filePathValidator;
    this.options = new Options();
  }

  public CommandProcessor addRequiredOption(String opt, String longOpt, boolean hasArg, String description) {
    options.addRequiredOption(opt, longOpt, hasArg, description);
    return this;
  }

  public CommandProcessor addOption(String opt, String longOpt, boolean hasArg, String description) {
    options.addOption(opt, longOpt, hasArg, description);
    return this;
  }

  public CommandLine parseArgs(String[] args) throws ParseException {
    CommandLineParser parser = new DefaultParser();
    return parser.parse(options, args);
  }

  public void printHelp(String cmdLineSyntax) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(cmdLineSyntax, options, true);
  }

  public void validateInputFile(String path, String fileType) throws InvalidFileException {
    File file = createFile(path);

    if (!file.exists()) {
      throw new InvalidFileException("The " + fileType + " file does not exist: " + path);
    }

    if (!file.isFile()) {
      throw new InvalidFileException("The " + fileType + " path must be a file: " + path);
    }

    if (!file.canRead()) {
      throw new FilePermissionException("Cannot read the " + fileType + " file (check permissions): " + path);
    }
  }

  public void validateOutputDir(String path) throws InvalidFileException {
    File dir = createFile(path);

    if (!dir.exists()) {
      throw new InvalidFileException("The output directory does not exist: " + path);
    }

    if (!dir.isDirectory()) {
      throw new InvalidFileException("The output path must be a directory: " + path);
    }

    if (!dir.canWrite()) {
      throw new FilePermissionException("Cannot write to the output directory (check permissions): " + path);
    }
  }

  public ValidationResult validateFilePath(String path) {
    return filePathValidator.validateFilePath(path);
  }

  @VisibleForTesting
  File createFile(String path) {
    return new File(path);
  }
}