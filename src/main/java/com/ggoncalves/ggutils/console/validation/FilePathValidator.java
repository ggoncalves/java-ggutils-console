package com.ggoncalves.ggutils.console.validation;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathValidator {

  public boolean isValidExistingFilePath(String filePath) {
    if (isEmptyOrNullFilePath(filePath)) return false;
    try {
      return Files.exists(Paths.get(filePath));
    }
    catch (Exception e) {
      // Invalid path syntax
      return false;
    }
  }

  public boolean isValidPathSyntax(String filePath) {
    if (isEmptyOrNullFilePath(filePath)) return false;

    try {
      Paths.get(filePath);
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  public ValidationResult validateFilePath(String filePath) {

    ValidationResult.ValidationResultBuilder validationResultBuilder = ValidationResult
        .builder().filePath(filePath);

    if (isEmptyOrNullFilePath(filePath)) {
      return validationResultBuilder
          .valid(false)
          .errorMessage("Path is null or empty")
          .build();
    }

    try {
      Path path = Paths.get(filePath);
      File file = path.toFile();

      boolean isFileExists = Files.exists(path);

      validationResultBuilder
          .valid(true)
          .exists(isFileExists);

      if (isFileExists) {
        validationResultBuilder
            .isDirectory(Files.isDirectory(path))
            .readable(file.canRead())
            .writable(file.canWrite())
            .executable(file.canExecute())
            .isBlank(file.length() == 0);
      }

    }
    catch (Exception e) {
      validationResultBuilder
          .valid(false)
          .errorMessage("Invalid path syntax: " + e.getMessage());
    }

    return validationResultBuilder.build();
  }

  private boolean isEmptyOrNullFilePath(String filePath) {
    return filePath == null || filePath.trim().isEmpty();
  }
}
