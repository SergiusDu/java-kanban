package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.RegularTaskCreationDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegularTaskCreationValidatorTest {

  private final Validator<RegularTaskCreationDTO> validator = new RegularTaskCreationValidator();

  @Test
  @DisplayName("validate should pass for valid RegularTaskCreationDTO")
  void validate_ValidRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO("ValidTitleXYZ", "ValidDescriptionXYZ");
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short title in RegularTaskCreationDTO")
  void validate_ShortTitleRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO("Short", "ValidDescriptionXYZ");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in RegularTaskCreationDTO")
  void validate_ShortDescriptionRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO("ValidTitleXYZ", "Short");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for multiple errors in RegularTaskCreationDTO")
  void validate_MultipleErrorsRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO("Short", "Short");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertEquals(2, exception.getErrors().size(), "Should contain two validation errors");
  }

  @Test
  @DisplayName("validate should throw NullPointerException when title is null")
  void validate_NullTitleThrowsException() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO(null, "ValidDescriptionXYZ");
    assertThrows(
        NullPointerException.class,
        () -> validator.validate(dto),
        "validate should throw NullPointerException when title is null");
  }

  @Test
  @DisplayName("validate should throw NullPointerException when description is null")
  void validate_NullDescriptionThrowsException() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO("ValidTitleXYZ", null);
    assertThrows(
        NullPointerException.class,
        () -> validator.validate(dto),
        "validate should throw NullPointerException when description is null");
  }
}
