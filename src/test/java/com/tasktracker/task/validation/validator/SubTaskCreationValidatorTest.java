package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.SubTaskCreationDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubTaskCreationValidatorTest {

  private final Validator<SubTaskCreationDTO> validator = new SubTaskCreationValidator();

  @Test
  @DisplayName("validate should pass for valid SubTaskCreationDTO")
  void validate_ValidSubTaskCreationDTO() {
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO(
            "ValidTitleXYZ", "ValidDescriptionXYZ", UUID.randomUUID(), null, null);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in SubTaskCreationDTO")
  void validate_ShortTitleSubTaskCreationDTO() {
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO("Short", "ValidDescriptionXYZ", UUID.randomUUID(), null, null);
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
      "validate should throw ValidationException for short description in SubTaskCreationDTO")
  void validate_ShortDescriptionSubTaskCreationDTO() {
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO("ValidTitleXYZ", "Short", UUID.randomUUID(), null, null);
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
      "validate should throw ValidationException for multiple errors in SubTaskCreationDTO")
  void validate_MultipleErrorsSubTaskCreationDTO() {
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO("Short", "Short", UUID.randomUUID(), null, null);
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
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO(null, "ValidDescriptionXYZ", UUID.randomUUID(), null, null);
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
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO("ValidTitleXYZ", null, UUID.randomUUID(), null, null);
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
