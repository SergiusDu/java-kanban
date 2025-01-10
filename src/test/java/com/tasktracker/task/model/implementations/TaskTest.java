package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JUnit5 tests for {@link Task} using {@link RegularTask} as the concrete implementation. These
 * tests ensure that the validation logic and behaviors defined in the {@link Task} class are
 * correctly enforced through the {@link RegularTask} implementation.
 */
class TaskTest {

  /** Tests that a Task (RegularTask) is successfully created with valid parameters. */
  @Test
  @DisplayName("Task constructor should create Task with valid parameters")
  void constructor_ValidParameters() {
    LocalDateTime creation = LocalDateTime.of(2023, 1, 1, 10, 0);
    LocalDateTime update = LocalDateTime.of(2023, 1, 2, 12, 0);
    RegularTask task =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    1,
                    "ValidTitleXYZ", // ≥ 10 characters
                    "ValidDescriptionXYZ", // ≥ 10 characters
                    TaskStatus.NEW,
                    creation,
                    update));

    assertEquals(1, task.getId(), "ID should be correctly assigned");
    assertEquals("ValidTitleXYZ", task.getTitle(), "Title should be correctly assigned");
    assertEquals(
        "ValidDescriptionXYZ", task.getDescription(), "Description should be correctly assigned");
    assertEquals(TaskStatus.NEW, task.getStatus(), "Status should be correctly assigned");
    assertEquals(creation, task.getCreationDate(), "Creation date should be correctly assigned");
    assertEquals(update, task.getUpdateDate(), "Update date should be correctly assigned");
  }

  /** Tests that a Task (RegularTask) cannot be created with an invalid (negative) ID. */
  @Test
  @DisplayName("Task constructor should throw ValidationException for invalid ID")
  void constructor_InvalidIdThrowsException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);

    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () ->
                new RegularTask(
                    -1, // Invalid ID
                    "ValidTitleXYZ",
                    "ValidDescriptionXYZ",
                    TaskStatus.NEW,
                    creation,
                    update),
            "Expected ValidationException for invalid ID");

    assertTrue(
        exception.getMessage().contains("The Task ID must be greater than 0"),
        "Exception message should indicate invalid ID");
  }

  /**
   * Tests that a Task (RegularTask) cannot be created with a title shorter than the minimum length.
   */
  @Test
  @DisplayName("Task constructor should throw ValidationException for short title")
  void constructor_ShortTitleThrowsException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);

    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () ->
                new RegularTask(
                    2,
                    "Short", // < 10 characters
                    "ValidDescriptionXYZ",
                    TaskStatus.IN_PROGRESS,
                    creation,
                    update),
            "Expected ValidationException for short title");

    assertTrue(
        exception.getMessage().contains("Title length should be greater than"),
        "Exception message should indicate short title");
  }

  /**
   * Tests that a Task (RegularTask) cannot be created with a description shorter than the minimum
   * length.
   */
  @Test
  @DisplayName("Task constructor should throw ValidationException for short description")
  void constructor_ShortDescriptionThrowsException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);

    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () ->
                new RegularTask(
                    3,
                    "ValidTitleXYZ",
                    "Short", // < 10 characters
                    TaskStatus.DONE,
                    creation,
                    update),
            "Expected ValidationException for short description");

    assertTrue(
        exception.getMessage().contains("Description length should be greater than"),
        "Exception message should indicate short description");
  }

  /** Tests that a Task (RegularTask) cannot be created with null values for non-nullable fields. */
  @Test
  @DisplayName("Task constructor should throw NullPointerException for null fields")
  void constructor_NullFieldsThrowsException() {
    LocalDateTime creation = LocalDateTime.now();
    LocalDateTime update = LocalDateTime.now().plusHours(1);

    // Null title
    NullPointerException titleException =
        assertThrows(
            NullPointerException.class,
            () ->
                new RegularTask(
                    4,
                    null, // Null title
                    "ValidDescriptionXYZ",
                    TaskStatus.NEW,
                    creation,
                    update),
            "Expected NullPointerException for null title");
    assertEquals(
        "Title can't be null.",
        titleException.getMessage(),
        "Exception message should match expected for null title");

    // Null description
    NullPointerException descriptionException =
        assertThrows(
            NullPointerException.class,
            () ->
                new RegularTask(
                    5,
                    "ValidTitleXYZ",
                    null, // Null description
                    TaskStatus.NEW,
                    creation,
                    update),
            "Expected NullPointerException for null description");
    assertEquals(
        "Description can't be null.",
        descriptionException.getMessage(),
        "Exception message should match expected for null description");

    // Null status
    NullPointerException statusException =
        assertThrows(
            NullPointerException.class,
            () ->
                new RegularTask(
                    6,
                    "ValidTitleXYZ",
                    "ValidDescriptionXYZ",
                    null, // Null status
                    creation,
                    update),
            "Expected NullPointerException for null status");
    assertEquals(
        "Task status can't be null.",
        statusException.getMessage(),
        "Exception message should match expected for null status");

    // Null creationDate
    NullPointerException creationDateException =
        assertThrows(
            NullPointerException.class,
            () ->
                new RegularTask(
                    7,
                    "ValidTitleXYZ",
                    "ValidDescriptionXYZ",
                    TaskStatus.NEW,
                    null, // Null creationDate
                    update),
            "Expected NullPointerException for null creationDate");
    assertEquals(
        "Date can be null.null",
        creationDateException.getMessage(),
        "Exception message should match expected for null creationDate");

    // Null updateDate
    NullPointerException updateDateException =
        assertThrows(
            NullPointerException.class,
            () ->
                new RegularTask(
                    8,
                    "ValidTitleXYZ",
                    "ValidDescriptionXYZ",
                    TaskStatus.NEW,
                    creation,
                    null), // Null updateDate
            "Expected NullPointerException for null updateDate");
    assertEquals(
        "Update date can't be null.",
        updateDateException.getMessage(),
        "Exception message should match expected for null updateDate");
  }

  /**
   * Tests that a Task (RegularTask) cannot be created with an updateDateTime earlier than
   * creationDateTime.
   */
  @Test
  @DisplayName(
      "Task constructor should throw ValidationException when updateDateTime is before creationDateTime")
  void constructor_UpdateDateBeforeCreationDateThrowsException() {
    LocalDateTime creation = LocalDateTime.of(2023, 1, 2, 10, 0);
    LocalDateTime update = LocalDateTime.of(2023, 1, 1, 9, 0); // Earlier than creationDate

    ValidationException exception =
        assertThrows(
            ValidationException.class,
            () ->
                new RegularTask(
                    9, "ValidTitleXYZ", "ValidDescriptionXYZ", TaskStatus.NEW, creation, update),
            "Expected ValidationException for updateDate before creationDate");

    assertTrue(
        exception.getMessage().contains("The update date can't be before creation date."),
        "Exception message should indicate updateDate before creationDate");
  }

  /** Tests the getter methods of Task (RegularTask) to ensure they return the correct values. */
  @Test
  @DisplayName("Task getters should return correct values")
  void getterMethods_ReturnCorrectValues() {
    LocalDateTime creation = LocalDateTime.of(2023, 2, 1, 8, 30);
    LocalDateTime update = LocalDateTime.of(2023, 2, 2, 9, 45);
    RegularTask task =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    10,
                    "AnotherValidTitle",
                    "AnotherValidDescription",
                    TaskStatus.IN_PROGRESS,
                    creation,
                    update));

    assertEquals(10, task.getId(), "getId should return the correct ID");
    assertEquals("AnotherValidTitle", task.getTitle(), "getTitle should return the correct title");
    assertEquals(
        "AnotherValidDescription",
        task.getDescription(),
        "getDescription should return the correct description");
    assertEquals(
        TaskStatus.IN_PROGRESS, task.getStatus(), "getStatus should return the correct status");
    assertEquals(
        creation,
        task.getCreationDate(),
        "getCreationDate should return the correct creation date");
    assertEquals(
        update, task.getUpdateDate(), "getUpdateDate should return the correct update date");
  }

  /** Tests the equals method to ensure that tasks with the same ID are considered equal. */
  @Test
  @DisplayName("Task equals should return true for tasks with the same ID")
  void equals_SameIdTasks() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 3, 1, 10, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 3, 2, 11, 0);
    RegularTask task1 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    11, "Task Title One", "DescriptionOne", TaskStatus.NEW, creation1, update1));

    LocalDateTime creation2 = LocalDateTime.of(2023, 4, 1, 12, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 4, 2, 13, 0);
    RegularTask task2 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    11, // Same ID as task1
                    "Task Title Two",
                    "DescriptionTwo",
                    TaskStatus.DONE,
                    creation2,
                    update2));

    assertEquals(task1, task2, "Tasks with the same ID should be equal");
  }

  /** Tests the equals method to ensure that tasks with different IDs are not considered equal. */
  @Test
  @DisplayName("Task equals should return false for tasks with different IDs")
  void equals_DifferentIdTasks() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 5, 1, 14, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 5, 2, 15, 0);
    RegularTask task1 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    12, "Task Title One", "DescriptionOne", TaskStatus.NEW, creation1, update1));

    LocalDateTime creation2 = LocalDateTime.of(2023, 6, 1, 16, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 6, 2, 17, 0);
    RegularTask task2 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    13, // Different ID
                    "Task Title Two",
                    "DescriptionTwo",
                    TaskStatus.DONE,
                    creation2,
                    update2));

    assertNotEquals(task1, task2, "Tasks with different IDs should not be equal");
  }

  /** Tests the hashCode method to ensure consistency with equals. */
  @Test
  @DisplayName("Task hashCode should be consistent with equals")
  void hashCode_ConsistencyWithEquals() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 7, 1, 18, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 7, 2, 19, 0);
    RegularTask task1 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    14, "Task Title One", "DescriptionOne", TaskStatus.NEW, creation1, update1));

    LocalDateTime creation2 = LocalDateTime.of(2023, 8, 1, 20, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 8, 2, 21, 0);
    RegularTask task2 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    14, // Same ID as task1
                    "Task Title Two",
                    "DescriptionTwo",
                    TaskStatus.DONE,
                    creation2,
                    update2));

    assertEquals(
        task1.hashCode(), task2.hashCode(), "hashCode should be equal for tasks with the same ID");
  }

  /** Tests the compareTo method to ensure tasks are ordered based on creationDate. */
  @Test
  @DisplayName("Task compareTo should order tasks based on creationDate")
  void compareTo_OrderBasedOnCreationDate() {
    LocalDateTime creation1 = LocalDateTime.of(2023, 10, 1, 8, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 10, 2, 9, 0);
    RegularTask task1 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    16, "Task Title One", "Description One", TaskStatus.NEW, creation1, update1));

    LocalDateTime creation2 = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 10, 2, 11, 0);
    RegularTask task2 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    17, "Task Title Two", "Description Two", TaskStatus.DONE, creation2, update2));

    // task1 was created earlier than task2
    assertTrue(task1.compareTo(task2) < 0, "task1 should be less than task2 based on creationDate");
    assertTrue(
        task2.compareTo(task1) > 0, "task2 should be greater than task1 based on creationDate");

    // task1 compared to itself should be 0
    assertEquals(0, task1.compareTo(task1), "task1 should be equal to itself");
  }

  /** Tests the compareTo method when tasks have the same creationDate. */
  @Test
  @DisplayName("Task compareTo should handle tasks with the same creationDate correctly")
  void compareTo_SameCreationDate() {
    LocalDateTime creation = LocalDateTime.of(2023, 11, 1, 10, 0);
    LocalDateTime update1 = LocalDateTime.of(2023, 11, 2, 11, 0);
    LocalDateTime update2 = LocalDateTime.of(2023, 11, 2, 12, 0);

    RegularTask task1 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    18, "Task Title One", "DescriptionOne", TaskStatus.NEW, creation, update1));

    RegularTask task2 =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    19, "Task Title Two", "DescriptionTwo", TaskStatus.DONE, creation, update2));

    // Since creation dates are the same, compareTo should return 0
    assertEquals(
        0, task1.compareTo(task2), "Tasks with the same creationDate should be equal in compareTo");
  }
}
