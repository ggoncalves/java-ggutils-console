package com.ggoncalves.ggutils.console.exception;

import lombok.Getter;

@Getter
public class InvalidFileException extends RuntimeException {

  public InvalidFileException(String message) {
    super(message);
  }
}