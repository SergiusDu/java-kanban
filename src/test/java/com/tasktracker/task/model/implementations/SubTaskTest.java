package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SubTaskTest {

  @Test
  @DisplayName("Constructor should create SubTask with valid parameters")
  void constructor_ValidParameters() {
    SubTask subTask =
        assertDoesNotThrow(
            () ->
                new SubTask(
                    1,
                    "ValidSubTitle",
                    "ValidSubDescription",
                    TaskStatus.NEW,
                    10,
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    assertEquals(1, subTask.getId());
    assertEquals("ValidSubTitle", subTask.getTitle());
    assertEquals("ValidSubDescription", subTask.getDescription());
    assertEquals(TaskStatus.NEW, subTask.getStatus());
    assertEquals(10, subTask.getEpicTaskId());
    assertNotNull(subTask.getCreationDate());
    assertNotNull(subTask.getUpdateDate());
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitleThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new SubTask(
                2,
                "Short",
                "ValidSubDescription",
                TaskStatus.IN_PROGRESS,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short description")
  void constructor_ShortDescriptionThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new SubTask(
                3,
                "ValidSubTitle",
                "Short",
                TaskStatus.DONE,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for negative epicTaskId")
  void constructor_NegativeEpicTaskIdThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new SubTask(
                4,
                "ValidSubTitle",
                "ValidSubDescription",
                TaskStatus.NEW,
                -5,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when title is null")
  void constructor_NullTitleThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                5,
                null,
                "ValidSubDescription",
                TaskStatus.NEW,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when description is null")
  void constructor_NullDescriptionThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                6,
                "ValidSubTitle",
                null,
                TaskStatus.NEW,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when status is null")
  void constructor_NullStatusThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                7,
                "ValidSubTitle",
                "ValidSubDescription",
                null,
                10,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when creationDateTime is null")
  void constructor_NullCreationDateTimeThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                8,
                "ValidSubTitle",
                "ValidSubDescription",
                TaskStatus.NEW,
                10,
                null,
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when updateDateTime is null")
  void constructor_NullUpdateDateTimeThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new SubTask(
                9,
                "ValidSubTitle",
                "ValidSubDescription",
                TaskStatus.NEW,
                10,
                LocalDateTime.now(),
                null));
  }

  @Test
  @DisplayName("getEpicTaskId should return the correct epicTaskId")
  void getEpicTaskId() {
    SubTask subTask =
        assertDoesNotThrow(
            () ->
                new SubTask(
                    10,
                    "AnotherValidSub",
                    "AnotherValidDesc",
                    TaskStatus.IN_PROGRESS,
                    20,
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    assertEquals(20, subTask.getEpicTaskId());
  }
}
