package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EpicTaskCreationValidatorTest {

  private final Validator<EpicTaskCreationDTO> validator = new EpicTaskCreationValidator();

  @Test
  @DisplayName("validate should pass for valid EpicTaskCreationDTO")
  void validate_ValidEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO("ValidTitleXYZ", "ValidDescriptionXYZ");
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in EpicTaskCreationDTO")
  void validate_ShortTitleEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO("Short", "ValidDescriptionXYZ");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in EpicTaskCreationDTO")
  void validate_ShortDescriptionEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO("ValidTitleXYZ", "Short");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for multiple errors in EpicTaskCreationDTO")
  void validate_MultipleErrorsEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO("Short", "Short");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertEquals(2, exception.getErrors().size(), "Should contain two validation errors");
  }
}
