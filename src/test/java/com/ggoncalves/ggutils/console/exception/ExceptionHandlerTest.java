package com.ggoncalves.ggutils.console.exception;

import com.ggoncalves.ggutils.console.test.util.TestAppender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ExceptionHandlerTest {

  // Capture System.err output
  private static TestAppender testAppender;

  @InjectMocks
  private ExceptionHandler exceptionHandler;

  @BeforeAll
  public static void setupBefore() {
    Logger logger = (Logger) LogManager.getRootLogger();
    testAppender = TestAppender.attachToLogger(logger);
  }

  @BeforeEach
  void setUpStreams() {
    testAppender.clear();
  }

  //  - tentar fazer a mesma coisa usando spy and mock with inject on logs
  private void assertTestAppenderMessage(String expectedMessage, Integer expectedMessageSize) {
    assertThat(testAppender.getMessages()).isNotEmpty();
    assertThat(testAppender.getMessages().size()).isEqualTo(expectedMessageSize);
    assertThat(testAppender.getMessages().get(0)).contains(expectedMessage);
  }

  private void assertTestAppenderMessage(String expectedMessage) {
    assertTestAppenderMessage(expectedMessage, 1);
  }

  @Nested
  @DisplayName("Tests for handling InvalidFileException")
  class InvalidFileExceptionTests {

    @Test
    @DisplayName("Should print to stderr for InvalidFileException")
    void shouldPrintInvalidFileException() {
      // Given
      String errorMessage = "File not found";
      InvalidFileException exception = new InvalidFileException(errorMessage);

      // When
      exceptionHandler.handle(exception);

      // Verify stderr output
      assertTestAppenderMessage("ERROR: The provided file is invalid - File not found");
    }
  }

  @Nested
  @DisplayName("Tests for handling FilePermissionException")
  class FilePermissionExceptionTests {

    @Test
    @DisplayName("Should print to stderr for FilePermissionException")
    void shouldPrintFilePermissionException() {
      // Given
      String errorMessage = "Access denied";
      FilePermissionException exception = new FilePermissionException(errorMessage);

      // When
      exceptionHandler.handle(exception);

      // Verify stderr output
      String formattedMessage = replacePlaceholders(
          ExceptionHandler.PERMISSION_ERROR_MESSAGE, "Access denied");

      assertTestAppenderMessage(formattedMessage);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private String replacePlaceholders(String originalMessage, String replacementMessage) {
    return originalMessage.replace("{}", replacementMessage);
  }

  @Nested
  @DisplayName("Tests for handling other exceptions")
  class OtherExceptionsTests {

    @Test
    @DisplayName("Should print to stderr for RuntimeException")
    void shouldPrintRuntimeException() {
      // Given
      String errorMessage = "General runtime error";
      RuntimeException exception = new RuntimeException(errorMessage);

      // When
      exceptionHandler.handle(exception);

      // Verify stderr output
      assertTestAppenderMessage(ExceptionHandler.UNEXPECTED_ERROR_MESSAGE + errorMessage, 2);
    }

    @Test
    @DisplayName("Should print to stderr for checked Exception")
    void shouldPrintCheckedException() {
      // Given
      String errorMessage = "IO error occurred";
      Exception exception = new Exception(errorMessage);

      // When
      exceptionHandler.handle(exception);

      // Verify stderr output
      assertTestAppenderMessage(ExceptionHandler.UNEXPECTED_ERROR_MESSAGE + errorMessage, 2);
//      assertThat(errContent.toString().trim())
//          .isEqualTo(ExceptionHandler.UNEXPECTED_ERROR_MESSAGE + errorMessage);
    }
  }

  @Nested
  @DisplayName("Tests for handling null exception")
  class NullExceptionTests {

    @Test
    @DisplayName("Should handle null exception gracefully")
    void shouldHandleNullException() {
      // Given
      Exception exception = new Exception((Throwable) null); // Exception with null message

      // When
      exceptionHandler.handle(exception);

      // Verify stderr output contains "null"
      assertTestAppenderMessage(ExceptionHandler.UNEXPECTED_ERROR_MESSAGE + "null", 2);
    }
  }
}