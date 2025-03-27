package com.ggoncalves.ggutils.console.cli;

import com.ggoncalves.ggutils.console.exception.FilePermissionException;
import com.ggoncalves.ggutils.console.exception.InvalidFileException;
import com.ggoncalves.ggutils.console.validation.FilePathValidator;
import com.ggoncalves.ggutils.console.validation.ValidationResult;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandProcessorTest {

  @Mock
  private FilePathValidator mockValidator;

  private CommandProcessor processor;
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

  @BeforeEach
  void setUp() {
    processor = new CommandProcessor(mockValidator);
    System.setOut(new PrintStream(outContent));
  }

  @Test
  @DisplayName("Should add required option to options")
  void shouldAddRequiredOptionToOptions() {
    // When
    CommandProcessor result = processor.addRequiredOption("t", "test", true, "Test option");

    // Then
    assertThat(result).isSameAs(processor);
  }

  @Test
  @DisplayName("Should add option to options")
  void shouldAddOptionToOptions() {
    // When
    CommandProcessor result = processor.addOption("t", "test", true, "Test option");

    // Then
    assertThat(result).isSameAs(processor);
  }

  @Test
  @DisplayName("Should print help message")
  void shouldPrintHelpMessage() {
    // Given
    processor.addOption("t", "test", true, "Test option");

    // When
    processor.printHelp("TestApp");

    // Then
    assertThat(outContent.toString()).contains("usage: TestApp");
    assertThat(outContent.toString()).contains("-t,--test");
  }

  @Nested
  @DisplayName("Tests for parseArgs method")
  class ParseArgsTests {

    @Test
    @DisplayName("Should parse valid arguments")
    void shouldParseValidArguments() throws ParseException {
      // Given
      processor.addOption("t", "test", true, "Test option");
      String[] args = {"-t", "value"};

      // When
      CommandLine result = processor.parseArgs(args);

      // Then
      assertThat(result.hasOption("t")).isTrue();
      assertThat(result.getOptionValue("t")).isEqualTo("value");
    }

    @Test
    @DisplayName("Should throw ParseException for Missing Option")
    void shouldThrowExceptionForMissingOption() {
      // Given
      processor.addRequiredOption("r", "required", true, "Required option");
      String[] args = {}; // Missing required option

      // Then
      assertThatThrownBy(() -> processor.parseArgs(args))
          .isInstanceOf(ParseException.class)
          .hasMessageContaining("Missing required option");
    }

    @Test
    @DisplayName("Should throw ParseException for Unrecognized Option")
    void shouldThrowExceptionForUnrecognizedOption() {

      // Given
      processor.addRequiredOption("r", "required", true, "Required option");
      String[] args = {"-t", "value"}; // Unrecognized option

      // Then
      assertThatThrownBy(() -> processor.parseArgs(args))
          .isInstanceOf(ParseException.class)
          .hasMessageContaining("Unrecognized option: -t");
    }
  }

  @Nested
  @DisplayName("Tests for file validation methods")
  class FileValidationTests {

    @Mock
    File mockFile;

    @Mock
    File mockDir;

    @Test
    @DisplayName("Should validate input file successfully")
    void shouldValidateInputFileSuccessfully() {
      // Given
      String path = "/valid/path/file.txt";
      when(mockFile.exists()).thenReturn(true);
      when(mockFile.isFile()).thenReturn(true);
      when(mockFile.canRead()).thenReturn(true);

      // Use spy to mock File constructor
      processor = spy(processor);
      doReturn(mockFile).when(processor).createFile(path);

      // When & Then
      assertThatCode(() -> processor.validateInputFile(path, "test"))
          .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should throw InvalidFileException when file does not exist")
    void shouldThrowInvalidFileExceptionWhenFileDoesNotExist() {
      // Given
      String path = "/invalid/path/file.txt";
      when(mockFile.exists()).thenReturn(false);

      // Use spy to mock File constructor
      processor = spy(processor);
      doReturn(mockFile).when(processor).createFile(path);
      // When & Then
      assertThatThrownBy(() -> processor.validateInputFile(path, "test"))
          .isInstanceOf(InvalidFileException.class)
          .hasMessageContaining("does not exist");
    }

    @Test
    @DisplayName("Should throw InvalidFileException when path is not a file")
    void shouldThrowInvalidFileExceptionWhenPathIsNotAFile() {
      // Given
      String path = "/valid/path/directory";
      when(mockFile.exists()).thenReturn(true);
      when(mockFile.isFile()).thenReturn(false);

      // Use spy to mock File constructor
      processor = spy(processor);
      doReturn(mockFile).when(processor).createFile(path);

      // When & Then
      assertThatThrownBy(() -> processor.validateInputFile(path, "test"))
          .isInstanceOf(InvalidFileException.class)
          .hasMessageContaining("must be a file");
    }

    @Test
    @DisplayName("Should throw FilePermissionException when file cannot be read")
    void shouldThrowFilePermissionExceptionWhenFileCannotBeRead() {
      // Given
      String path = "/valid/path/unreadable.txt";
      when(mockFile.exists()).thenReturn(true);
      when(mockFile.isFile()).thenReturn(true);
      when(mockFile.canRead()).thenReturn(false);

      // Use spy to mock File constructor
      processor = spy(processor);
      doReturn(mockFile).when(processor).createFile(path);

      // When & Then
      assertThatThrownBy(() -> processor.validateInputFile(path, "test"))
          .isInstanceOf(FilePermissionException.class)
          .hasMessageContaining("Cannot read");
    }

    @Test
    @DisplayName("Should validate output directory successfully")
    void shouldValidateOutputDirectorySuccessfully() {
      // Given
      String path = "/valid/output/dir";
      when(mockDir.exists()).thenReturn(true);
      when(mockDir.isDirectory()).thenReturn(true);
      when(mockDir.canWrite()).thenReturn(true);

      // Use spy to mock File constructor
      processor = spy(processor);
      doReturn(mockDir).when(processor).createFile(path);

      // When & Then
      assertThatCode(() -> processor.validateOutputDir(path))
          .doesNotThrowAnyException();
    }
  }

  @Test
  @DisplayName("Should delegate to FilePathValidator when validating file path")
  void shouldDelegateToFilePathValidatorWhenValidatingFilePath() {
    // Given
    String path = "/some/path/file.txt";
    ValidationResult expectedResult = ValidationResult.builder().build();
    when(mockValidator.validateFilePath(path)).thenReturn(expectedResult);

    // When
    ValidationResult result = processor.validateFilePath(path);

    // Then
    assertThat(result).isSameAs(expectedResult);
    verify(mockValidator).validateFilePath(path);
  }
}