package com.tasktracker.task.validation.validator;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.SubTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.validation.Validator;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubTaskUpdateValidatorTest {

  private final Validator<SubTaskUpdateDTO> validator = new SubTaskUpdateValidator();

  private static final UUID VALID_TASK_ID = UUID.randomUUID();
  private static final UUID VALID_EPIC_ID = UUID.randomUUID();
  private static final String VALID_TITLE = "Valid Title With Enough Characters";
  private static final String VALID_DESCRIPTION = "Valid Description Also With Enough Characters";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.now().plusDays(1);
  private static final Duration DEFAULT_DURATION = Duration.ofHours(2);

  @Test
  @DisplayName("validate should pass for valid SubTaskUpdateDTO")
  void validate_ValidSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            VALID_TASK_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            VALID_EPIC_ID,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass for valid SubTaskUpdateDTO with null times")
  void validate_ValidSubTaskUpdateDTO_NullTimes() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            VALID_TASK_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.IN_PROGRESS,
            VALID_EPIC_ID,
            null,
            null);
    assertDoesNotThrow(() -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should throw ValidationException for short title in SubTaskUpdateDTO")
  void validate_ShortTitleSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            UUID.randomUUID(),
            SHORT_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.IN_PROGRESS,
            VALID_EPIC_ID,
            null,
            null);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")),
        "Exception should contain title length error message. Actual: " + exception.getErrors());
  }

  @Test
  @DisplayName(
      "validate should throw ValidationException for short description in SubTaskUpdateDTO")
  void validate_ShortDescriptionSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            UUID.randomUUID(),
            VALID_TITLE,
            SHORT_DESCRIPTION,
            TaskStatus.DONE,
            VALID_EPIC_ID,
            null,
            null);
    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dto));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")),
        "Exception should contain description length error message. Actual: "
            + exception.getErrors());
  }

  @Test
  @DisplayName("validate should throw NullPointerException for null epicId in SubTaskUpdateDTO")
  void validate_NullEpicIdSubTaskUpdateDTO() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            UUID.randomUUID(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.NEW, null, null, null);
    assertThrows(
        NullPointerException.class,
        () -> validator.validate(dto),
        "Expected NullPointerException because CommonValidationUtils.validateUuid will throw it for null epicId before validator collects errors list.");
  }

  @Test
  @DisplayName("validate should throw ValidationException for multiple errors in SubTaskUpdateDTO")
  void validate_MultipleErrorsSubTaskUpdateDTO() {
    SubTaskUpdateDTO dtoWithShortTitleAndDesc =
        new SubTaskUpdateDTO(
            UUID.randomUUID(),
            SHORT_TITLE,
            SHORT_DESCRIPTION,
            TaskStatus.NEW,
            VALID_EPIC_ID,
            null,
            null);

    ValidationException exception =
        assertThrows(ValidationException.class, () -> validator.validate(dtoWithShortTitleAndDesc));
    assertEquals(
        2,
        exception.getErrors().size(),
        "Exception should contain two validation errors (title, description). Actual: "
            + exception.getErrors());
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Title length should be at least")));
    assertTrue(
        exception.getErrors().stream()
            .anyMatch(error -> error.contains("Description length should be at least")));
  }

  @Test
  @DisplayName("validate should throw NullPointerException when title is null in SubTaskUpdateDTO")
  void validate_NullTitleThrowsException() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            UUID.randomUUID(), null, VALID_DESCRIPTION, TaskStatus.NEW, VALID_EPIC_ID, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName(
      "validate should throw NullPointerException when description is null in SubTaskUpdateDTO")
  void validate_NullDescriptionThrowsException() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            UUID.randomUUID(), VALID_TITLE, null, TaskStatus.NEW, VALID_EPIC_ID, null, null);
    assertThrows(NullPointerException.class, () -> validator.validate(dto));
  }

  @Test
  @DisplayName("validate should pass when status is null (status not validated by this validator)")
  void validate_NullStatus_PassesValidation() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            VALID_TASK_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            null, // Status
            VALID_EPIC_ID,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);
    assertDoesNotThrow(
        () -> validator.validate(dto),
        "Null status should not cause validation failure in SubTaskUpdateValidator as it's not checked there.");
  }

  @Test
  @DisplayName("validate should pass when id is null (id not validated by this validator)")
  void validate_NullId_PassesValidation() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            null, // ID
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            VALID_EPIC_ID,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);
    assertDoesNotThrow(
        () -> validator.validate(dto),
        "Null id should not cause validation failure in SubTaskUpdateValidator as it's not checked there.");
  }
}
