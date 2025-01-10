package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.RegularTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** JUnit5 tests for {@link RegularTaskUpdateValidator}. */
class RegularTaskUpdateValidatorTest {

  private final Validator<RegularTaskUpdateDTO> validator = new RegularTaskUpdateValidator();

  @Test
  @DisplayName("validate should pass for valid RegularTaskUpdateDTO")
  void validate_ValidRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            1,
            "ValidTitleXYZ", // ≥ 10 characters
            "ValidDescriptionXYZ", // ≥ 10 characters
            TaskStatus.NEW);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in RegularTaskUpdateDTO")
  void validate_ShortTitleRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            2,
            "Short", // < 10 characters
            "ValidDescriptionXYZ",
            TaskStatus.IN_PROGRESS);
    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () -> validator.validate(dto),
            "Expected ValidationException for short title");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message");
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in RegularTaskUpdateDTO")
  void validate_ShortDescriptionRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            3,
            "ValidTitleXYZ",
            "Short", // < 10 characters
            TaskStatus.DONE);
    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () -> validator.validate(dto),
            "Expected ValidationException for short description");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")),
        "Exception should contain description length error message");
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for multiple errors in RegularTaskUpdateDTO")
  void validate_MultipleErrorsRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            4,
            "Short", // < 10 characters
            "Short", // < 10 characters
            TaskStatus.NEW);
    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () -> validator.validate(dto),
            "Expected ValidationException for multiple validation errors");
    assertEquals(2, exception.getErrors().size(), "Exception should contain two validation errors");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")),
        "Exception should contain description length error message");
  }

  @Test
  @DisplayName("validate should throw NullPointerException when title is null")
  void validate_NullTitleThrowsException() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            5,
            null, // Null title
            "ValidDescriptionXYZ",
            TaskStatus.NEW);
    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> validator.validate(dto),
            "Expected NullPointerException for null title");
    assertEquals(
        "Title can't be null.",
        exception.getMessage(),
        "Exception message should match expected for null title");
  }

  @Test
  @DisplayName("validate should throw NullPointerException when description is null")
  void validate_NullDescriptionThrowsException() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            6,
            "ValidTitleXYZ",
            null, // Null description
            TaskStatus.NEW);
    NullPointerException exception =
        assertThrows(
            NullPointerException.class,
            () -> validator.validate(dto),
            "Expected NullPointerException for null description");
    assertEquals(
        "Description can't be null.",
        exception.getMessage(),
        "Exception message should match expected for null description");
  }
}
