package com.tasktracker.task.exception;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** JUnit5 tests for {@link ValidationException}. */
class ValidationExceptionTest {

  @Test
  @DisplayName("ValidationException(List<String>) should correctly set message and errors")
  void constructor_ListOfErrors_SetsMessageAndErrors() {
    List<String> errors = Arrays.asList("Error1", "Error2", "Error3");
    ValidationException exception = new ValidationException(errors);

    String expectedMessage = "Validation failed: Error1, Error2, Error3";
    assertEquals(
        expectedMessage, exception.getMessage(), "Exception message should concatenate all errors");

    assertNotNull(exception.getErrors(), "getErrors should return the provided list of errors");
    assertEquals(
        errors, exception.getErrors(), "getErrors should return the exact list of errors provided");
  }

  @Test
  @DisplayName("ValidationException(String) should correctly set message and set errors to null")
  void constructor_SingleError_SetsMessageAndNullErrors() {
    String error = "Single error message";
    ValidationException exception = new ValidationException(error);

    assertEquals(
        error, exception.getMessage(), "Exception message should match the single error provided");

    assertNull(
        exception.getErrors(), "getErrors should return null when constructed with a single error");
  }

  @Test
  @DisplayName("ValidationException should be an instance of RuntimeException")
  void validationException_IsRuntimeException() {
    ValidationException exceptionWithList =
        new ValidationException(Collections.singletonList("Error"));
    ValidationException exceptionWithSingleError = new ValidationException("Single error");

    assertInstanceOf(
        RuntimeException.class,
        exceptionWithList,
        "ValidationException should extend RuntimeException");
    assertInstanceOf(
        RuntimeException.class,
        exceptionWithSingleError,
        "ValidationException should extend RuntimeException");
  }

  @Test
  @DisplayName("ValidationException(List<String>) should handle empty error list")
  void constructor_EmptyErrorList_SetsEmptyMessageAndEmptyErrors() {
    List<String> emptyErrors = Collections.emptyList();
    ValidationException exception = new ValidationException(emptyErrors);

    String expectedMessage = "Validation failed: ";
    assertEquals(
        expectedMessage,
        exception.getMessage(),
        "Exception message should handle empty error list");

    assertNotNull(exception.getErrors(), "getErrors should not return null for empty error list");
    assertTrue(
        exception.getErrors().isEmpty(),
        "getErrors should return an empty list when constructed with an empty list");
  }

  @Test
  @DisplayName(
      "ValidationException(List<String>) should throw NullPointerException when errors list is null")
  void constructor_NullErrorList_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new ValidationException((List<String>) null),
        "Expected NullPointerException when constructed with null error list");
  }

  /**
   * Tests that the ValidationException handles a single null error message. Ensures that the
   * exception message is set to "null" and that getErrors remains null.
   */
  @Test
  @DisplayName("ValidationException(String) should handle null error message")
  void constructor_NullSingleError_SetsMessageToNullAndErrorsToNull() {
    ValidationException exception = new ValidationException((String) null);

    assertNull(
        exception.getMessage(), "Exception message should be null when constructed with null");
    assertNull(
        exception.getErrors(),
        "getErrors should return null when constructed with a null error message");
  }
}
