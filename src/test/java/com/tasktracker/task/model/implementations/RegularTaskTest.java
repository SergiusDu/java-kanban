package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegularTaskTest {

  @Test
  @DisplayName("Constructor should create RegularTask with valid parameters")
  void constructor_ValidParameters() {
    RegularTask task =
        assertDoesNotThrow(
            () ->
                new RegularTask(
                    1,
                    "ValidTitleXYZ",
                    "ValidDescriptionXYZ",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()));

    assertEquals(1, task.getId());
    assertEquals("ValidTitleXYZ", task.getTitle());
    assertEquals("ValidDescriptionXYZ", task.getDescription());
    assertEquals(TaskStatus.NEW, task.getStatus());
    assertNotNull(task.getCreationDate());
    assertNotNull(task.getUpdateDate());
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitleThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new RegularTask(
                2,
                "Short",
                "ValidDescriptionXYZ",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short description")
  void constructor_ShortDescriptionThrowsException() {
    assertThrows(
        ValidationException.class,
        () ->
            new RegularTask(
                3,
                "ValidTitleXYZ",
                "Short",
                TaskStatus.DONE,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when title is null")
  void constructor_NullTitleThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                4,
                null,
                "ValidDescriptionXYZ",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when description is null")
  void constructor_NullDescriptionThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                5,
                "ValidTitleXYZ",
                null,
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when status is null")
  void constructor_NullStatusThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                6,
                "ValidTitleXYZ",
                "ValidDescriptionXYZ",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when creationDateTime is null")
  void constructor_NullCreationDateTimeThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                7,
                "ValidTitleXYZ",
                "ValidDescriptionXYZ",
                TaskStatus.NEW,
                null,
                LocalDateTime.now()));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when updateDateTime is null")
  void constructor_NullUpdateDateTimeThrowsException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new RegularTask(
                8,
                "ValidTitleXYZ",
                "ValidDescriptionXYZ",
                TaskStatus.NEW,
                LocalDateTime.now(),
                null));
  }
}
