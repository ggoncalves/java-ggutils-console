package com.ggoncalves.ggutils.console.validation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResult {
  private String filePath;
  private boolean valid;
  private boolean exists;
  private boolean isDirectory;
  private boolean readable;
  private boolean writable;
  private boolean executable;
  private String errorMessage;
  private boolean isBlank;
}