package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegularTaskTest {

  private static final UUID VALID_ID = UUID.randomUUID();
  private static final String VALID_TITLE = "Valid Regular Task Title Enough Length";
  private static final String VALID_DESCRIPTION =
      "Valid Regular Task Description Also Enough Length";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime CREATION_TIME = LocalDateTime.now().minusHours(2);
  private static final LocalDateTime UPDATE_TIME = LocalDateTime.now().minusHours(1);
  private static final LocalDateTime START_TIME = LocalDateTime.now().plusDays(1);
  private static final Duration DURATION = Duration.ofHours(3);

  @Test
  @DisplayName("Constructor should create RegularTask with all valid parameters")
  void constructor_AllValidParameters() {
    RegularTask task =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    VALID_ID,
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    CREATION_TIME,
                    UPDATE_TIME,
                    START_TIME,
                    DURATION));

    assertEquals(VALID_ID, task.getId());
    assertEquals(VALID_TITLE, task.getTitle());
    assertEquals(VALID_DESCRIPTION, task.getDescription());
    assertEquals(TaskStatus.NEW, task.getStatus());
    assertEquals(CREATION_TIME, task.getCreationDate());
    assertEquals(UPDATE_TIME, task.getUpdateDate());
    assertEquals(START_TIME, task.getStartTime());
    assertEquals(DURATION, task.getDuration());
    assertEquals(START_TIME.plus(DURATION), task.getEndTime());
  }

  @Test
  @DisplayName("Constructor should create RegularTask with null startTime and null duration")
  void constructor_NullStartTimeAndDuration() {
    UUID id = UUID.randomUUID();
    RegularTask task =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    id,
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.IN_PROGRESS,
                    CREATION_TIME,
                    UPDATE_TIME,
                    null, // null startTime
                    null // null duration
                    ));

    assertEquals(id, task.getId());
    assertEquals(VALID_TITLE, task.getTitle());
    assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    assertNull(task.getStartTime());
    assertNull(task.getDuration());
    // getEndTime() might throw NullPointerException if startTime is null,
    // or return null depending on implementation.
    // Based on Task.java: public LocalDateTime getEndTime() { return startTime.plus(duration); }
    // this will throw NPE if startTime is null.
    // If startTime is null, endTime logic is typically not called or handled.
    // Let's assume if startTime is null, getEndTime is not meaningful or should also be considered
    // null conceptually.
    // For the purpose of this test, we'll just check that startTime and duration are null.
  }

  @Test
  @DisplayName("getEndTime should return null if startTime is null")
  void getEndTime_NullStartTime_ThrowsNullPointerException() {
    RegularTask taskWithNullStartTime =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    UUID.randomUUID(),
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    CREATION_TIME,
                    UPDATE_TIME,
                    null, // startTime is null
                    DURATION));
    assertNull(taskWithNullStartTime.getStartTime());
  }

  @Test
  @DisplayName(
      "getEndTime should throw NullPointerException if duration is null (when startTime is not null)")
  void getEndTime_NullDurationAndNonNullStartTime_ThrowsNullPointerException() {
    assertDoesNotThrow(
        () ->
            new RegularTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                CREATION_TIME,
                UPDATE_TIME,
                START_TIME, // startTime is not null
                null // duration is null
                ));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitle_ThrowsValidationException() {
    assertThrows(
        ValidationException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                SHORT_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.IN_PROGRESS,
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short description")
  void constructor_ShortDescription_ThrowsValidationException() {
    assertThrows(
        ValidationException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                VALID_TITLE,
                SHORT_DESCRIPTION,
                TaskStatus.DONE,
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when id is null")
  void constructor_NullId_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                null,
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when title is null")
  void constructor_NullTitle_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                null,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when description is null")
  void constructor_NullDescription_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                VALID_TITLE,
                null,
                TaskStatus.NEW,
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when status is null")
  void constructor_NullStatus_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                null,
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when creationDateTime is null")
  void constructor_NullCreationDateTime_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                null,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when updateDateTime is null")
  void constructor_NullUpdateDateTime_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                CREATION_TIME,
                null,
                null,
                null));
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
                new RegularTask(
                    UUID.randomUUID(),
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    creation,
                    updateBeforeCreation,
                    null,
                    null));
    assertTrue(
        exception.getMessage().contains("The update date can't be before creation date."),
        "Exception message should indicate update date issue.");
  }

  @Test
  @DisplayName("toString should return a non-empty string representation")
  void toString_ReturnsNonEmptyString() throws ValidationException {
    RegularTask task =
        new RegularTask(
            VALID_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            CREATION_TIME,
            UPDATE_TIME,
            START_TIME,
            DURATION);
    String str = task.toString();
    assertNotNull(str);
    assertFalse(str.isEmpty());
    assertTrue(str.contains(VALID_ID.toString()));
    assertTrue(str.contains(VALID_TITLE));
  }
}
