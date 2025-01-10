package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EpicTaskUpdateValidatorTest {

  private final Validator<EpicTaskUpdateDTO> validator = new EpicTaskUpdateValidator();

  @Test
  @DisplayName("validate should pass for valid EpicTaskUpdateDTO")
  void validate_ValidEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(1, "ValidDescriptionXYZ", "Descrition is Valid");
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in EpicTaskUpdateDTO")
  void validate_ShortTitleEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(2, "Short", "ValidDescriptionXYZ");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in EpicTaskUpdateDTO")
  void validate_ShortDescriptionEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(3, "ValidTitleXYZ", "Short");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")));
  }

  @Test
  @DisplayName("validate should throw ValidationException for multiple errors in EpicTaskUpdateDTO")
  void validate_MultipleErrorsEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(4, "Short", "Short");
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertEquals(2, exception.getErrors().size(), "Should contain two validation errors");
  }
}
