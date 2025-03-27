package com.ggoncalves.ggutils.console.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class FilePathValidatorTest {

  @InjectMocks
  private FilePathValidator filePathValidator;

  @TempDir
  private Path tempDir;

  private Path existingFile;
  private Path nonExistentFile;
  private Path existingDirectory;
  private Path readOnlyFile;
  private Path notEmptyFile;

  @BeforeEach
  void setUp() throws IOException {
    // Create test files and directories
    existingDirectory = tempDir;
    existingFile = tempDir.resolve("test-file.txt");
    nonExistentFile = tempDir.resolve("non-existent-file.txt");
    readOnlyFile = tempDir.resolve("read-only-file.txt");
    notEmptyFile = tempDir.resolve("not-empty-file.txt");


    // Create the files
    Files.createFile(existingFile);
    Files.createFile(readOnlyFile);
    Files.write(notEmptyFile, "Some content".getBytes());

    // Make read-only file actually read-only
    File readOnlyFileObj = readOnlyFile.toFile();

    //noinspection ResultOfMethodCallIgnored (this being a test, we just want to set regardless of success)
    readOnlyFileObj.setWritable(false);
  }

  @Nested
  @DisplayName("Tests for isValidExistingFilePath")
  class IsValidExistingFilePathTests {


    @Test
    @DisplayName("Should return false for null path")
    void shouldReturnFalseForNullPath() {
      assertThat(filePathValidator.isValidExistingFilePath(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return false for empty path")
    void shouldReturnFalseForEmptyPath(String path) {
      assertThat(filePathValidator.isValidExistingFilePath(path)).isFalse();
    }

    @Test
    @DisplayName("Should return true for existing file")
    void shouldReturnTrueForExistingFile() {
      assertThat(filePathValidator.isValidExistingFilePath(existingFile.toString())).isTrue();
    }

    @Test
    @DisplayName("Should return true for non-empty file")
    void shouldReturnTrueForNotEmptyFile() {
      assertThat(filePathValidator.isValidExistingFilePath(notEmptyFile.toString())).isTrue();
    }

    @Test
    @DisplayName("Should return true for existing directory")
    void shouldReturnTrueForExistingDirectory() {
      assertThat(filePathValidator.isValidExistingFilePath(existingDirectory.toString())).isTrue();
    }

    @Test
    @DisplayName("Should return false for non-existent file")
    void shouldReturnFalseForNonExistentFile() {
      assertThat(filePathValidator.isValidExistingFilePath(nonExistentFile.toString())).isFalse();
    }

    @Test
    @DisplayName("Should return false for invalid path syntax")
    void shouldReturnFalseForInvalidPathSyntax() {
      // On Windows, colons are invalid in file names except for drive letter
      // On Unix-like systems, null character is invalid
      String invalidPath = System.getProperty("os.name")
          .toLowerCase()
          .contains("win") ? "C:\\invalid\\path\\with:illegal:character" : "/invalid/path/with/\0nullcharacter";

      assertThat(filePathValidator.isValidExistingFilePath(invalidPath)).isFalse();
    }

    @Test
    @DisplayName("Should handle exceptions thrown by Paths.get")
    void shouldHandleExceptionsFromPathsGet() {
      try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
        mockedPaths.when(() -> Paths.get(any(String.class)))
            .thenThrow(new RuntimeException("Mocked exception"));

        assertThat(filePathValidator.isValidExistingFilePath("/some/path")).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("Tests for isValidPathSyntax")
  class IsValidPathSyntaxTests {

    @Test
    @DisplayName("Should return false for null path")
    void shouldReturnFalseForNullPath() {
      assertThat(filePathValidator.isValidPathSyntax(null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return false for empty paths")
    void shouldReturnFalseForEmptyPaths(String path) {
      assertThat(filePathValidator.isValidPathSyntax(path)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/valid/path", "C:\\valid\\path", "./relative/path", "../parent/path"})
    @DisplayName("Should return true for valid path syntax")
    void shouldReturnTrueForValidPathSyntax(String path) {
      assertThat(filePathValidator.isValidPathSyntax(path)).isTrue();
    }

    @Test
    @DisplayName("Should handle exceptions thrown by Paths.get")
    void shouldHandleExceptionsFromPathsGet() {
      try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
        mockedPaths.when(() -> Paths.get(any(String.class)))
            .thenThrow(new RuntimeException("Mocked exception"));

        assertThat(filePathValidator.isValidPathSyntax("/some/path")).isFalse();
      }
    }
  }

  @Nested
  @DisplayName("Tests for validateFilePath")
  class ValidateFilePathTests {

    @Test
    @DisplayName("Should return invalid result for null path")
    void shouldReturnInvalidResultForNullPath() {
      ValidationResult result = filePathValidator.validateFilePath(null);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrorMessage()).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return invalid result for empty paths")
    void shouldReturnInvalidResultForEmptyPaths(String path) {
      ValidationResult result = filePathValidator.validateFilePath(path);

      assertThat(result.isValid()).isFalse();
      assertThat(result.getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return valid result with correct attributes for existing file")
    void shouldReturnValidResultWithCorrectAttributesForExistingFile() {
      ValidationResult result = filePathValidator.validateFilePath(existingFile.toString());

      assertThat(result.isValid()).isTrue();
      assertThat(result.isExists()).isTrue();
      assertThat(result.isDirectory()).isFalse();
      assertThat(result.isReadable()).isTrue();
      assertThat(result.isWritable()).isTrue();
      assertThat(result.isBlank()).isTrue();
      assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should return valid result with correct attributes for not empty file")
    void shouldReturnValidResultWithCorrectAttributesForNotEmptyFile() {
      ValidationResult result = filePathValidator.validateFilePath(notEmptyFile.toString());

      assertThat(result.isValid()).isTrue();
      assertThat(result.isExists()).isTrue();
      assertThat(result.isDirectory()).isFalse();
      assertThat(result.isReadable()).isTrue();
      assertThat(result.isWritable()).isTrue();
      assertThat(result.isBlank()).isFalse();
      assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should return valid result with correct attributes for existing directory")
    void shouldReturnValidResultWithCorrectAttributesForExistingDirectory() {
      ValidationResult result = filePathValidator.validateFilePath(existingDirectory.toString());

      assertThat(result.isValid()).isTrue();
      assertThat(result.isExists()).isTrue();
      assertThat(result.isDirectory()).isTrue();
      assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should return valid result with exists=false for non-existent file")
    void shouldReturnValidResultWithExistsFalseForNonExistentFile() {
      ValidationResult result = filePathValidator.validateFilePath(nonExistentFile.toString());

      assertThat(result.isValid()).isTrue();
      assertThat(result.isExists()).isFalse();
      assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should return correct writable attribute for read-only file")
    void shouldReturnCorrectWritableAttributeForReadOnlyFile() {
      ValidationResult result = filePathValidator.validateFilePath(readOnlyFile.toString());

      assertThat(result.isValid()).isTrue();
      assertThat(result.isExists()).isTrue();
      assertThat(result.isWritable()).isFalse();
      assertThat(result.isReadable()).isTrue();
    }

    @Test
    @DisplayName("Should handle exceptions thrown during validation")
    void shouldHandleExceptionsDuringValidation() {
      try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class)) {
        mockedPaths.when(() -> Paths.get(any(String.class)))
            .thenThrow(new RuntimeException("Mocked exception"));

        ValidationResult result = filePathValidator.validateFilePath("/some/path");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Mocked exception");
      }
    }
  }

  @Nested
  @DisplayName("Tests for ValidationResult class")
  class ValidationResultTests {

    @Test
    @DisplayName("Should correctly use getters and setters")
    void shouldCorrectlyUseGettersAndSetters() {
      ValidationResult result = new ValidationResult.ValidationResultBuilder().build();

      result.setValid(true);
      result.setExists(true);
      result.setDirectory(true);
      result.setReadable(true);
      result.setWritable(true);
      result.setExecutable(true);
      result.setBlank(true);
      result.setErrorMessage("Test error");

      assertThat(result.isValid()).isTrue();
      assertThat(result.isExists()).isTrue();
      assertThat(result.isDirectory()).isTrue();
      assertThat(result.isReadable()).isTrue();
      assertThat(result.isWritable()).isTrue();
      assertThat(result.isExecutable()).isTrue();
      assertThat(result.isBlank()).isTrue();
      assertThat(result.getErrorMessage()).isEqualTo("Test error");
    }
  }
}