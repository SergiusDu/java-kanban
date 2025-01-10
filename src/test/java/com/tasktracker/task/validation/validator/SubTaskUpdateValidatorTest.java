package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.SubTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** JUnit5 tests for {@link SubTaskUpdateValidator}. */
class SubTaskUpdateValidatorTest {

  private final Validator<SubTaskUpdateDTO> validator = new SubTaskUpdateValidator();

  @Test
  @DisplayName("validate should pass for valid SubTaskUpdateDTO")
  void validate_ValidSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            1,
            "ValidTitleXYZ", // ≥ 10 characters
            "ValidDescriptionXYZ", // ≥ 10 characters
            TaskStatus.NEW,
            10);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in SubTaskUpdateDTO")
  void validate_ShortTitleSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            2,
            "Short", // < 10 characters
            "ValidDescriptionXYZ",
            TaskStatus.IN_PROGRESS,
            10);
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
      "validate should throw ValidationException for short description in SubTaskUpdateDTO")
  void validate_ShortDescriptionSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            3,
            "ValidTitleXYZ",
            "Short", // < 10 characters
            TaskStatus.DONE,
            10);
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
  @DisplayName("validate should throw ValidationException for negative epicId in SubTaskUpdateDTO")
  void validate_NegativeEpicIdSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            4, "ValidTitleXYZ", "ValidDescriptionXYZ", TaskStatus.NEW, -5 // Negative epicId
            );
    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () -> validator.validate(dto),
            "Expected ValidationException for negative epicId");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Epic com.tasktracker.task ID should be positive.")),
        "Exception should contain epicId positivity error message");
  }

  @Test
  @DisplayName("validate should throw ValidationException for multiple errors in SubTaskUpdateDTO")
  void validate_MultipleErrorsSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            5,
            "Short", // < 10 characters
            "Short", // < 10 characters
            TaskStatus.NEW,
            -10 // Negative epicId
            );
    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () -> validator.validate(dto),
            "Expected ValidationException for multiple validation errors");
    assertEquals(
        3, exception.getErrors().size(), "Exception should contain three validation errors");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")),
        "Exception should contain description length error message");
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Epic com.tasktracker.task ID should be positive.")),
        "Exception should contain epicId positivity error message");
  }

  @Test
  @DisplayName("validate should throw NullPointerException when title is null in SubTaskUpdateDTO")
  void validate_NullTitleThrowsException() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            6,
            null, // Null title
            "ValidDescriptionXYZ",
            TaskStatus.NEW,
            10);
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
  @DisplayName(
      "validate should throw NullPointerException when description is null in SubTaskUpdateDTO")
  void validate_NullDescriptionThrowsException() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            7,
            "ValidTitleXYZ",
            null, // Null description
            TaskStatus.NEW,
            10);
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
