package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubTaskTest {

  // Constants for test data
  private static final UUID VALID_ID = UUID.randomUUID();
  private static final UUID VALID_EPIC_ID = UUID.randomUUID();
  private static final String VALID_TITLE = "Valid SubTask Title With Sufficient Length";
  private static final String VALID_DESCRIPTION =
      "Valid SubTask Description Also With Sufficient Length";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime CREATION_TIME = LocalDateTime.now().minusHours(1);
  private static final LocalDateTime UPDATE_TIME = LocalDateTime.now();
  private static final LocalDateTime START_TIME = LocalDateTime.now().plusDays(1);
  private static final Duration DURATION = Duration.ofHours(2);

  @Test
  @DisplayName("Constructor should create SubTask with all valid parameters")
  void constructor_AllValidParameters() {
    SubTask subTask =
        assertDoesNotThrow(
            () ->
                new SubTask(
                    VALID_ID,
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    VALID_EPIC_ID,
                    CREATION_TIME,
                    UPDATE_TIME,
                    START_TIME,
                    DURATION));

    assertEquals(VALID_ID, subTask.getId());
    assertEquals(VALID_TITLE, subTask.getTitle());
    assertEquals(VALID_DESCRIPTION, subTask.getDescription());
    assertEquals(TaskStatus.NEW, subTask.getStatus());
    assertEquals(VALID_EPIC_ID, subTask.getEpicTaskId());
    assertEquals(CREATION_TIME, subTask.getCreationDate());
    assertEquals(UPDATE_TIME, subTask.getUpdateDate());
    assertEquals(START_TIME, subTask.getStartTime());
    assertEquals(DURATION, subTask.getDuration());
  }

  @Test
  @DisplayName("Constructor should create SubTask with null startTime and null duration")
  void constructor_NullStartTimeAndDuration() {
    UUID id = UUID.randomUUID();
    UUID epicId = UUID.randomUUID();
    SubTask subTask =
        assertDoesNotThrow(
            () ->
                new SubTask(
                    id,
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.IN_PROGRESS,
                    epicId,
                    CREATION_TIME,
                    UPDATE_TIME,
                    null, // null startTime
                    null // null duration
                    ));

    assertEquals(id, subTask.getId());
    assertEquals(VALID_TITLE, subTask.getTitle());
    assertEquals(TaskStatus.IN_PROGRESS, subTask.getStatus());
    assertEquals(epicId, subTask.getEpicTaskId());
    assertNull(subTask.getStartTime());
    assertNull(subTask.getDuration());
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitle_ThrowsValidationException() {
    assertThrows(
        ValidationException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                SHORT_TITLE, // Invalid title
                VALID_DESCRIPTION,
                TaskStatus.IN_PROGRESS,
                VALID_EPIC_ID,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short description")
  void constructor_ShortDescription_ThrowsValidationException() {
    assertThrows(
        ValidationException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                VALID_TITLE,
                SHORT_DESCRIPTION, // Invalid description
                TaskStatus.DONE,
                VALID_EPIC_ID,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when epicTaskId is null")
  void constructor_NullEpicTaskId_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                null, // null epicTaskId
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when id is null")
  void constructor_NullId_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                null, // null id
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                VALID_EPIC_ID,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when title is null")
  void constructor_NullTitle_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                null, // null title
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                VALID_EPIC_ID,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when description is null")
  void constructor_NullDescription_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                VALID_TITLE,
                null, // null description
                TaskStatus.NEW,
                VALID_EPIC_ID,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when status is null")
  void constructor_NullStatus_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                null, // null status
                VALID_EPIC_ID,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when creationDateTime is null")
  void constructor_NullCreationDateTime_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                VALID_EPIC_ID,
                null, // null creationDateTime
                UPDATE_TIME,
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when updateDateTime is null")
  void constructor_NullUpdateDateTime_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                VALID_EPIC_ID,
                CREATION_TIME,
                null, // null updateDateTime
                START_TIME,
                DURATION));
  }

  @Test
  @DisplayName(
      "Constructor should throw ValidationException if updateDateTime is before creationDateTime")
  void constructor_UpdateBeforeCreation_ThrowsValidationException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime updateBeforeCreation = creation.minusDays(1);
    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () ->
                new SubTask(
                    UUID.randomUUID(),
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    VALID_EPIC_ID,
                    creation,
                    updateBeforeCreation, // update time before creation time
                    START_TIME,
                    DURATION));
    assertTrue(
        exception.getMessage().contains("The update date can't be before creation date."),
        "Exception message should indicate update date issue.");
  }

  @Test
  @DisplayName("getEpicTaskId should return the correct epicTaskId")
  void getEpicTaskId_ReturnsCorrectId() throws ValidationException {
    UUID specificEpicId = UUID.randomUUID();
    SubTask subTask =
        new SubTask(
            UUID.randomUUID(),
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.IN_PROGRESS,
            specificEpicId, // Specific epicId for this test
            CREATION_TIME,
            UPDATE_TIME,
            null,
            null);

    assertEquals(specificEpicId, subTask.getEpicTaskId());
  }

  @Test
  @DisplayName("toString should return a non-empty string representation")
  void toString_ReturnsNonEmptyString() throws ValidationException {
    SubTask subTask =
        new SubTask(
            VALID_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            VALID_EPIC_ID,
            CREATION_TIME,
            UPDATE_TIME,
            START_TIME,
            DURATION);
    String str = subTask.toString();
    assertNotNull(str);
    assertFalse(str.isEmpty());
    assertTrue(str.contains(VALID_ID.toString()));
    assertTrue(str.contains(VALID_TITLE));
    assertTrue(str.contains(VALID_EPIC_ID.toString()));
  }
}
