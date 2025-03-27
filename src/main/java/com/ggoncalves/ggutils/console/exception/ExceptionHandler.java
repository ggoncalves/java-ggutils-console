package com.ggoncalves.ggutils.console.exception;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ExceptionHandler {

  public static final String INVALID_FILE_ERROR = "ERROR: The provided file is invalid - {}";
  public static final String PERMISSION_ERROR_MESSAGE = "Permission error: {}";
  public static final String UNEXPECTED_ERROR_MESSAGE = "ERROR: An unexpected error occurred - ";

  public void handle(Throwable e) {
    if (e instanceof InvalidFileException) {
      log.error(INVALID_FILE_ERROR, e.getMessage());
    }
    else if (e instanceof FilePermissionException) {
      log.error(PERMISSION_ERROR_MESSAGE, e.getMessage());
    }
    else {
      log.error(UNEXPECTED_ERROR_MESSAGE + e.getMessage(), e);
    }

    if (!(e instanceof InvalidFileException) && !(e instanceof FilePermissionException)) {
      log.debug("Stack trace:", e);
    }
  }
}