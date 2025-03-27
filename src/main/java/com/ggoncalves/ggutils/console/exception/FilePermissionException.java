package com.ggoncalves.ggutils.console.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FilePermissionException extends RuntimeException {

  public FilePermissionException(String message) {
    super(message);
  }
}