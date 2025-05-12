package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.EpicTaskCreationDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EpicTaskCreationValidatorTest {

  private final Validator<EpicTaskCreationDTO> validator = new EpicTaskCreationValidator();

  private static final String VALID_TITLE = "Valid Epic Title With Enough Characters";
  private static final String VALID_DESCRIPTION =
      "Valid Epic Description Also With Enough Characters";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.now().plusDays(1);

  @Test
  @DisplayName("validate should pass for valid EpicTaskCreationDTO with startTime")
  void validate_ValidEpicTaskCreationDTO_WithStartTime() {
    EpicTaskCreationDTO dto =
        new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, DEFAULT_START_TIME);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass for valid EpicTaskCreationDTO with null startTime")
  void validate_ValidEpicTaskCreationDTO_NullStartTime() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, null);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in EpicTaskCreationDTO")
  void validate_ShortTitleEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(SHORT_TITLE, VALID_DESCRIPTION, null);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message. Actual: " + exception.getErrors());
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in EpicTaskCreationDTO")
  void validate_ShortDescriptionEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(VALID_TITLE, SHORT_DESCRIPTION, null);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")),
        "Exception should contain description length error message. Actual: "
            + exception.getErrors());
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for multiple errors in EpicTaskCreationDTO")
  void validate_MultipleErrorsEpicTaskCreationDTO() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(SHORT_TITLE, SHORT_DESCRIPTION, null);
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
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(null, VALID_DESCRIPTION, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw NullPointerException when description is null")
  void validate_NullDescriptionThrowsException() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(VALID_TITLE, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }
}
