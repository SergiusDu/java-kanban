package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.RegularTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.validation.Validator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegularTaskUpdateValidatorTest {

  private final Validator<RegularTaskUpdateDTO> validator = new RegularTaskUpdateValidator();

  private static final UUID VALID_TASK_ID = UUID.randomUUID();
  private static final String VALID_TITLE = "Valid Title With Enough Characters";
  private static final String VALID_DESCRIPTION = "Valid Description Also With Enough Characters";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.now().plusHours(1);
  private static final Duration DEFAULT_DURATION = Duration.ofMinutes(90);

  @Test
  @DisplayName("validate should pass for valid RegularTaskUpdateDTO with all fields")
  void validate_ValidRegularTaskUpdateDTO_AllFields() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            VALID_TASK_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass for valid RegularTaskUpdateDTO with null times")
  void validate_ValidRegularTaskUpdateDTO_NullTimes() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            VALID_TASK_ID, VALID_TITLE, VALID_DESCRIPTION, TaskStatus.IN_PROGRESS, null, null);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in RegularTaskUpdateDTO")
  void validate_ShortTitleRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            UUID.randomUUID(), SHORT_TITLE, VALID_DESCRIPTION, TaskStatus.IN_PROGRESS, null, null);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message. Actual: " + exception.getErrors());
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in RegularTaskUpdateDTO")
  void validate_ShortDescriptionRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            UUID.randomUUID(), VALID_TITLE, SHORT_DESCRIPTION, TaskStatus.DONE, null, null);
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
      "validate should throw ValidationException for multiple errors in RegularTaskUpdateDTO")
  void validate_MultipleErrorsRegularTaskUpdateDTO() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            UUID.randomUUID(), SHORT_TITLE, SHORT_DESCRIPTION, TaskStatus.NEW, null, null);
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
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            UUID.randomUUID(), null, VALID_DESCRIPTION, TaskStatus.NEW, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw NullPointerException when description is null")
  void validate_NullDescriptionThrowsException() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(UUID.randomUUID(), VALID_TITLE, null, TaskStatus.NEW, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass when id is null (id not validated by this validator)")
  void validate_NullId_PassesValidation() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            null, // id
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);
    assertDoesNotThrow(
        () -> validator.validate(dto),
        "Null id should not cause validation failure in RegularTaskUpdateValidator.");
  }

  @Test
  @DisplayName("validate should pass when status is null (status not validated by this validator)")
  void validate_NullStatus_PassesValidation() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            VALID_TASK_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            null, // status
            DEFAULT_START_TIME,
            DEFAULT_DURATION);
    assertDoesNotThrow(
        () -> validator.validate(dto),
        "Null status should not cause validation failure in RegularTaskUpdateValidator.");
  }
}
