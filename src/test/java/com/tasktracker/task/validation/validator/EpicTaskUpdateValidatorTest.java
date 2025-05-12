package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.EpicTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EpicTaskUpdateValidatorTest {

  private final Validator<EpicTaskUpdateDTO> validator = new EpicTaskUpdateValidator();

  private static final UUID VALID_TASK_ID = UUID.randomUUID();
  private static final String VALID_TITLE = "Valid Epic Title With Enough Characters";
  private static final String VALID_DESCRIPTION =
      "Valid Epic Description Also With Enough Characters";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";

  @Test
  @DisplayName("validate should pass for valid EpicTaskUpdateDTO")
  void validate_ValidEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(VALID_TASK_ID, VALID_TITLE, VALID_DESCRIPTION);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in EpicTaskUpdateDTO")
  void validate_ShortTitleEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto =
        new EpicTaskUpdateDTO(UUID.randomUUID(), SHORT_TITLE, VALID_DESCRIPTION);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message. Actual: " + exception.getErrors());
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in EpicTaskUpdateDTO")
  void validate_ShortDescriptionEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto =
        new EpicTaskUpdateDTO(UUID.randomUUID(), VALID_TITLE, SHORT_DESCRIPTION);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")),
        "Exception should contain description length error message. Actual: "
            + exception.getErrors());
  }

  @Test
  @DisplayName("validate should throw ValidationException for multiple errors in EpicTaskUpdateDTO")
  void validate_MultipleErrorsEpicTaskUpdateDTO() {
    EpicTaskUpdateDTO dto =
        new EpicTaskUpdateDTO(UUID.randomUUID(), SHORT_TITLE, SHORT_DESCRIPTION);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertEquals(
        2,
        exception.getErrors().size(),
        "Should contain two validation errors (title, description). Actual: "
            + exception.getErrors());
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")));
  }

  @Test
  @DisplayName("validate should throw NullPointerException when title is null")
  void validate_NullTitleThrowsException() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(UUID.randomUUID(), null, VALID_DESCRIPTION);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw NullPointerException when description is null")
  void validate_NullDescriptionThrowsException() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(UUID.randomUUID(), VALID_TITLE, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass when id is null (id not validated by this validator)")
  void validate_NullId_PassesValidation() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(null, VALID_TITLE, VALID_DESCRIPTION);
    assertDoesNotThrow(
        () -> validator.validate(dto),
        "Null id should not cause validation failure in EpicTaskUpdateValidator.");
  }
}
