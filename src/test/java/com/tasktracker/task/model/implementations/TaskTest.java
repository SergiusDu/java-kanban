package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskTest {

  private static final UUID VALID_ID_1 = UUID.randomUUID();
  private static final UUID VALID_ID_2 = UUID.randomUUID();
  private static final String VALID_TITLE_SUFFIX = "Valid Task Title";
  private static final String VALID_DESCRIPTION_SUFFIX = "Valid Task Description";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.now().plusDays(1);
  private static final Duration DEFAULT_DURATION = Duration.ofHours(2);

  private RegularTask createTask(
      UUID id,
      String title,
      String description,
      TaskStatus status,
      LocalDateTime creation,
      LocalDateTime update,
      LocalDateTime startTime,
      Duration duration) {
    try {
      return new RegularTask(id, title, description, status, creation, update, startTime, duration);
    } catch (ValidationException e) {
      fail("Task creation failed with ValidationException: " + e.getMessage());
    }
    return null; // Should not be reached
  }

  @Test
  @DisplayName("Constructor should create Task with all valid parameters")
  void constructor_ValidParameters() {
    LocalDateTime creation = LocalDateTime.of(2023, 1, 1, 10, 0);
    LocalDateTime update = LocalDateTime.of(2023, 1, 2, 12, 0);
    RegularTask task =
        createTask(
            VALID_ID_1,
            VALID_TITLE_SUFFIX + " Valid",
            VALID_DESCRIPTION_SUFFIX + " Valid",
            TaskStatus.NEW,
            creation,
            update,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);

    assertNotNull(task);
    assertEquals(VALID_ID_1, task.getId());
    assertEquals(VALID_TITLE_SUFFIX + " Valid", task.getTitle());
    assertEquals(VALID_DESCRIPTION_SUFFIX + " Valid", task.getDescription());
    assertEquals(TaskStatus.NEW, task.getStatus());
    assertEquals(creation, task.getCreationDate());
    assertEquals(update, task.getUpdateDate());
    assertEquals(DEFAULT_START_TIME, task.getStartTime());
    assertEquals(DEFAULT_DURATION, task.getDuration());
    assertEquals(DEFAULT_START_TIME.plus(DEFAULT_DURATION), task.getEndTime());
  }

  @Test
  @DisplayName("Constructor should create Task with null startTime and null duration")
  void constructor_NullStartTimeAndDuration() {
    LocalDateTime creation = LocalDateTime.of(2023, 1, 1, 10, 0);
    LocalDateTime update = LocalDateTime.of(2023, 1, 2, 12, 0);
    RegularTask task =
        createTask(
            VALID_ID_1,
            VALID_TITLE_SUFFIX + " NullTime",
            VALID_DESCRIPTION_SUFFIX + " NullTime",
            TaskStatus.IN_PROGRESS,
            creation,
            update,
            null,
            null);
    assertNotNull(task);
    assertNull(task.getStartTime());
    assertNull(task.getDuration());
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitle_ThrowsValidationException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        ValidationException.class,
        () ->
            new RegularTask(
                VALID_ID_2,
                SHORT_TITLE,
                VALID_DESCRIPTION_SUFFIX + " ShortTitle",
                TaskStatus.IN_PROGRESS,
                creation,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short description")
  void constructor_ShortDescription_ThrowsValidationException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        ValidationException.class,
        () ->
            new RegularTask(
                VALID_ID_1,
                VALID_TITLE_SUFFIX + " ShortDesc",
                SHORT_DESCRIPTION,
                TaskStatus.DONE,
                creation,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException for null id")
  void constructor_NullId_ThrowsNullPointerException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                null,
                VALID_TITLE_SUFFIX + " NullId",
                VALID_DESCRIPTION_SUFFIX + " NullId",
                TaskStatus.NEW,
                creation,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException for null title")
  void constructor_NullTitle_ThrowsNullPointerException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                VALID_ID_1,
                null,
                VALID_DESCRIPTION_SUFFIX + " NullTitle",
                TaskStatus.NEW,
                creation,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException for null description")
  void constructor_NullDescription_ThrowsNullPointerException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                VALID_ID_1,
                VALID_TITLE_SUFFIX + " NullDesc",
                null,
                TaskStatus.NEW,
                creation,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException for null status")
  void constructor_NullStatus_ThrowsNullPointerException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                VALID_ID_1,
                VALID_TITLE_SUFFIX + " NullStatus",
                VALID_DESCRIPTION_SUFFIX + " NullStatus",
                null,
                creation,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException for null creationDate")
  void constructor_NullCreationDate_ThrowsNullPointerException() {
    LocalDateTime update = LocalDateTime.now().plusHours(1);
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                VALID_ID_1,
                VALID_TITLE_SUFFIX + " NullCreateDate",
                VALID_DESCRIPTION_SUFFIX + " NullCreateDate",
                TaskStatus.NEW,
                null,
                update,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException for null updateDate")
  void constructor_NullUpdateDate_ThrowsNullPointerException() {
    LocalDateTime creation = LocalDateTime.now();
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                VALID_ID_1,
                VALID_TITLE_SUFFIX + " NullUpdateDate",
                VALID_DESCRIPTION_SUFFIX + " NullUpdateDate",
                TaskStatus.NEW,
                creation,
                null,
                null,
                null));
  }

  @Test
  @DisplayName(
      "Constructor should throw ValidationException when updateDate is before creationDate")
  void constructor_UpdateDateBeforeCreationDate_ThrowsValidationException() {
    LocalDateTime creation = LocalDateTime.of(2023, 1, 2, 10, 0);
    LocalDateTime updateBeforeCreation = LocalDateTime.of(2023, 1, 1, 9, 0);

    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () ->
                new RegularTask(
                    VALID_ID_1,
                    VALID_TITLE_SUFFIX + " UpdateBeforeCreate",
                    VALID_DESCRIPTION_SUFFIX + " UpdateBeforeCreate",
                    TaskStatus.NEW,
                    creation,
                    updateBeforeCreation,
                    null,
                    null));
    assertTrue(exception.getMessage().contains("The update date can't be before creation date."));
  }

  @Test
  @DisplayName("Getter methods should return correct values")
  void getterMethods_ReturnCorrectValues() {
    LocalDateTime creation = LocalDateTime.of(2023, 2, 1, 8, 30);
    LocalDateTime update = LocalDateTime.of(2023, 2, 2, 9, 45);
    RegularTask task =
        createTask(
            VALID_ID_2,
            VALID_TITLE_SUFFIX + " Getters",
            VALID_DESCRIPTION_SUFFIX + " Getters",
            TaskStatus.IN_PROGRESS,
            creation,
            update,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);

    assertNotNull(task);
    assertEquals(VALID_ID_2, task.getId());
    assertEquals(VALID_TITLE_SUFFIX + " Getters", task.getTitle());
    assertEquals(VALID_DESCRIPTION_SUFFIX + " Getters", task.getDescription());
    assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
    assertEquals(creation, task.getCreationDate());
    assertEquals(update, task.getUpdateDate());
    assertEquals(DEFAULT_START_TIME, task.getStartTime());
    assertEquals(DEFAULT_DURATION, task.getDuration());
    assertEquals(DEFAULT_START_TIME.plus(DEFAULT_DURATION), task.getEndTime());
  }

  @Test
  @DisplayName("equals should return true for tasks with the same ID")
  void equals_SameIdTasks() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 3, 1, 10, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 3, 2, 11, 0);
    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "DescriptionOne",
            TaskStatus.NEW,
            creation1,
            update1,
            null,
            null);

    LocalDateTime creation2 = LocalDateTime.of(2023, 4, 1, 12, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 4, 2, 13, 0);
    RegularTask task2 =
        createTask(
            VALID_ID_1,
            "Task Title Two",
            "DescriptionTwo",
            TaskStatus.DONE,
            creation2,
            update2,
            null,
            null);

    assertNotNull(task1);
    assertNotNull(task2);
    assertEquals(task1, task2);
  }

  @Test
  @DisplayName("equals should return false for tasks with different IDs")
  void equals_DifferentIdTasks() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 5, 1, 14, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 5, 2, 15, 0);
    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "DescriptionOne",
            TaskStatus.NEW,
            creation1,
            update1,
            null,
            null);

    LocalDateTime creation2 = LocalDateTime.of(2023, 6, 1, 16, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 6, 2, 17, 0);
    RegularTask task2 =
        createTask(
            VALID_ID_2,
            "Task Title Two",
            "DescriptionTwo",
            TaskStatus.DONE,
            creation2,
            update2,
            null,
            null);

    assertNotNull(task1);
    assertNotNull(task2);
    assertNotEquals(task1, task2);
  }

  @Test
  @DisplayName("equals should return false when comparing with null")
  void equals_CompareWithNull() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 5, 1, 14, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 5, 2, 15, 0);
    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "DescriptionOne",
            TaskStatus.NEW,
            creation1,
            update1,
            null,
            null);
    assertNotNull(task1);
    assertNotEquals(null, task1);
  }

  @Test
  @DisplayName("equals should return false when comparing with different object type")
  void equals_CompareWithDifferentType() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 5, 1, 14, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 5, 2, 15, 0);
    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "DescriptionOne",
            TaskStatus.NEW,
            creation1,
            update1,
            null,
            null);
    assertNotNull(task1);
    assertNotEquals("A String Object", task1);
  }

  @Test
  @DisplayName("hashCode should be consistent with equals")
  void hashCode_ConsistencyWithEquals() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 7, 1, 18, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 7, 2, 19, 0);
    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "DescriptionOne",
            TaskStatus.NEW,
            creation1,
            update1,
            null,
            null);

    LocalDateTime creation2 = LocalDateTime.of(2023, 8, 1, 20, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 8, 2, 21, 0);
    RegularTask task2 =
        createTask(
            VALID_ID_1,
            "Task Title Two",
            "DescriptionTwo",
            TaskStatus.DONE,
            creation2,
            update2,
            null,
            null);

    assertNotNull(task1);
    assertNotNull(task2);
    assertEquals(task1.hashCode(), task2.hashCode());
  }

  @Test
  @DisplayName("compareTo should order tasks based on creationDate")
  void compareTo_OrderBasedOnCreationDate() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 10, 1, 8, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 10, 2, 9, 0);
    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "Description One",
            TaskStatus.NEW,
            creation1,
            update1,
            null,
            null);

    LocalDateTime creation2 = LocalDateTime.of(2023, 10, 1, 10, 0); // Later creation
    LocalDateTime update2 = LocalDateTime.of(2023, 10, 2, 11, 0);
    RegularTask task2 =
        createTask(
            VALID_ID_2,
            "Task Title Two",
            "Description Two",
            TaskStatus.DONE,
            creation2,
            update2,
            null,
            null);

    assertNotNull(task1);
    assertNotNull(task2);
    assertTrue(task1.compareTo(task2) < 0);
    assertTrue(task2.compareTo(task1) > 0);
    assertEquals(0, task1.compareTo(task1));
  }

  @Test
  @DisplayName("compareTo should handle tasks with the same creationDate correctly")
  void compareTo_SameCreationDate() {
    LocalDateTime creation = LocalDateTime.of(2023, 11, 1, 10, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 11, 2, 11, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 11, 2, 12, 0);

    RegularTask task1 =
        createTask(
            VALID_ID_1,
            "Task Title One",
            "DescriptionOne",
            TaskStatus.NEW,
            creation,
            update1,
            null,
            null);
    RegularTask task2 =
        createTask(
            VALID_ID_2,
            "Task Title Two",
            "DescriptionTwo",
            TaskStatus.DONE,
            creation,
            update2,
            null,
            null);

    assertNotNull(task1);
    assertNotNull(task2);
    assertEquals(0, task1.compareTo(task2));
  }
}
