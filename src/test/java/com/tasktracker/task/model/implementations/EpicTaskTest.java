package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EpicTaskTest {

  @Test
  @DisplayName("Constructor should create EpicTask with valid parameters")
  void constructor_ValidParameters() {
    EpicTask epic =
        assertDoesNotThrow(
            () ->
                new EpicTask(
                    1,
                    "ValidTitleX", // ≥ 10 chars
                    "ValidDescripY", // ≥ 10 chars
                    TaskStatus.NEW,
                    Set.of(10, 20),
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    assertEquals(1, epic.getId());
    assertEquals("ValidTitleX", epic.getTitle());
    assertEquals("ValidDescripY", epic.getDescription());
    assertEquals(TaskStatus.NEW, epic.getStatus());
    assertEquals(2, epic.getSubtaskIds().size());
    assertTrue(epic.getSubtaskIds().contains(10));
    assertTrue(epic.getSubtaskIds().contains(20));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitleThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new EpicTask(
                1,
                "Short", // < 10 chars
                "ValidDescripY",
                TaskStatus.NEW,
                Set.of(),
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short description")
  void constructor_ShortDescriptionThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new EpicTask(
                1,
                "ValidTitleX",
                "ShortDesc", // < 10 chars
                TaskStatus.NEW,
                Set.of(),
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for negative subtask IDs")
  void constructor_NegativeSubtaskIdsThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new EpicTask(
                1,
                "ValidTitleX",
                "ValidDescripY",
                TaskStatus.NEW,
                Set.of(-1, 2, 3), // contains a negative ID
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException if any required argument is null")
  void constructor_NullArguments() {
    // Null title
    assertThrows(
        NullPointerException.class,
        () ->
            new EpicTask(
                1,
                null,
                "ValidDescripY",
                TaskStatus.NEW,
                Set.of(),
                LocalDateTime.now(),
                LocalDateTime.now()));

    // Null description
    assertThrows(
        NullPointerException.class,
        () ->
            new EpicTask(
                1,
                "ValidTitleX",
                null,
                TaskStatus.NEW,
                Set.of(),
                LocalDateTime.now(),
                LocalDateTime.now()));

    // Null status
    assertThrows(
        NullPointerException.class,
        () ->
            new EpicTask(
                1,
                "ValidTitleX",
                "ValidDescripY",
                null,
                Set.of(),
                LocalDateTime.now(),
                LocalDateTime.now()));

    // Null creationDate
    assertThrows(
        NullPointerException.class,
        () ->
            new EpicTask(
                1,
                "ValidTitleX",
                "ValidDescripY",
                TaskStatus.NEW,
                Set.of(),
                null,
                LocalDateTime.now()));

    // Null updateDate
    assertThrows(
        NullPointerException.class,
        () ->
            new EpicTask(
                1,
                "ValidTitleX",
                "ValidDescripY",
                TaskStatus.NEW,
                Set.of(),
                LocalDateTime.now(),
                null));
  }

  @Test
  @DisplayName("getSubtaskIds should return an unmodifiable copy of subtask IDs")
  void getSubtaskIds() {
    EpicTask epic =
        assertDoesNotThrow(
            () ->
                new EpicTask(
                    2,
                    "AnotherValidX",
                    "AnotherValidD",
                    TaskStatus.IN_PROGRESS,
                    Set.of(100, 200),
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    Set<Integer> subtaskIds = epic.getSubtaskIds();
    assertEquals(2, subtaskIds.size());
    assertTrue(subtaskIds.contains(100));
    assertTrue(subtaskIds.contains(200));

    // Attempt to modify the returned set should fail or not affect the original
    assertThrows(UnsupportedOperationException.class, () -> subtaskIds.add(300));
    assertEquals(2, epic.getSubtaskIds().size(), "Original subtask IDs should remain unchanged");
  }
}
