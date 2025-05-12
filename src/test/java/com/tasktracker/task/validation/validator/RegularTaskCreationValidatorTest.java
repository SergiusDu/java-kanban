package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.RegularTaskCreationDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.validation.Validator;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegularTaskCreationValidatorTest {

  private final Validator<RegularTaskCreationDTO> validator = new RegularTaskCreationValidator();

  private static final String VALID_TITLE = "Valid Title With Enough Characters";
  private static final String VALID_DESCRIPTION = "Valid Description Also With Enough Characters";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.now().plusHours(1);
  private static final Duration DEFAULT_DURATION = Duration.ofMinutes(90);

  @Test
  @DisplayName("validate should pass for valid RegularTaskCreationDTO with time")
  void validate_ValidRegularTaskCreationDTO_WithTime() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(
            VALID_TITLE, VALID_DESCRIPTION, DEFAULT_START_TIME, DEFAULT_DURATION);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass for valid RegularTaskCreationDTO with null times")
  void validate_ValidRegularTaskCreationDTO_NullTimes() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, null, null);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short title in RegularTaskCreationDTO")
  void validate_ShortTitleRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(SHORT_TITLE, VALID_DESCRIPTION, null, null);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message. Actual: " + exception.getErrors());
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in RegularTaskCreationDTO")
  void validate_ShortDescriptionRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(VALID_TITLE, SHORT_DESCRIPTION, null, null);
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
      "validate should throw ValidationException for multiple errors in RegularTaskCreationDTO")
  void validate_MultipleErrorsRegularTaskCreationDTO() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(SHORT_TITLE, SHORT_DESCRIPTION, null, null);
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
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO(null, VALID_DESCRIPTION, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw NullPointerException when description is null")
  void validate_NullDescriptionThrowsException() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO(VALID_TITLE, null, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass even if startTime is null and duration is not (or vice-versa)")
  void validate_PartialNullTimes_PassesValidation() {
    RegularTaskCreationDTO dtoWithNullStart =
        new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, null, DEFAULT_DURATION);
    assertDoesNotThrow(
        () -> validator.validate(dtoWithNullStart),
        "Validation should pass if only startTime is null, as this validator doesn't check time fields.");

    RegularTaskCreationDTO dtoWithNullDuration =
        new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, DEFAULT_START_TIME, null);
    assertDoesNotThrow(
        () -> validator.validate(dtoWithNullDuration),
        "Validation should pass if only duration is null, as this validator doesn't check time fields.");
  }
}
