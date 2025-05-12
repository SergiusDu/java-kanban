package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EpicTaskTest {

  private static final UUID VALID_ID = UUID.randomUUID();
  private static final String VALID_TITLE = "Valid Epic Title With Sufficient Length";
  private static final String VALID_DESCRIPTION =
      "Valid Epic Description Also With Sufficient Length";
  private static final String SHORT_TITLE = "Short";
  private static final String SHORT_DESCRIPTION = "Brief";
  private static final LocalDateTime CREATION_TIME = LocalDateTime.now().minusHours(2);
  private static final LocalDateTime UPDATE_TIME = LocalDateTime.now().minusHours(1);
  private static final LocalDateTime START_TIME = LocalDateTime.now().plusDays(1);
  private static final Duration DURATION = Duration.ofHours(4);
  private static final Set<UUID> VALID_SUBTASK_IDS =
      Stream.of(UUID.randomUUID(), UUID.randomUUID()).collect(Collectors.toSet());

  @Test
  @DisplayName("Constructor should create EpicTask with all valid parameters")
  void constructor_AllValidParameters() {
    EpicTask epic =
        assertDoesNotThrow(
            () ->
                new EpicTask(
                    VALID_ID,
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    VALID_SUBTASK_IDS,
                    CREATION_TIME,
                    UPDATE_TIME,
                    START_TIME,
                    DURATION));

    assertEquals(VALID_ID, epic.getId());
    assertEquals(VALID_TITLE, epic.getTitle());
    assertEquals(VALID_DESCRIPTION, epic.getDescription());
    assertEquals(TaskStatus.NEW, epic.getStatus());
    assertEquals(VALID_SUBTASK_IDS.size(), epic.getSubtaskIds().size());
    assertTrue(
        epic.getSubtaskIds().containsAll(VALID_SUBTASK_IDS),
        "Epic should contain all provided subtask IDs");
    assertEquals(CREATION_TIME, epic.getCreationDate());
    assertEquals(UPDATE_TIME, epic.getUpdateDate());
    assertEquals(START_TIME, epic.getStartTime());
    assertEquals(DURATION, epic.getDuration());
  }

  @Test
  @DisplayName("Constructor should create EpicTask with empty subtask set and null times")
  void constructor_EmptySubtasksNullTimes() {
    UUID id = UUID.randomUUID();
    EpicTask epic =
        assertDoesNotThrow(
            () ->
                new EpicTask(
                    id,
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.IN_PROGRESS,
                    Set.of(), // Empty set of subtasks
                    CREATION_TIME,
                    UPDATE_TIME,
                    null, // null startTime
                    null // null duration
                    ));

    assertEquals(id, epic.getId());
    assertEquals(VALID_TITLE, epic.getTitle());
    assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    assertTrue(epic.getSubtaskIds().isEmpty(), "Subtask IDs set should be empty");
    assertNull(epic.getStartTime());
    assertNull(epic.getDuration());
  }

  @Test
  @DisplayName("Constructor should throw ValidationException for short title")
  void constructor_ShortTitle_ThrowsValidationException() {
    assertThrows(
        ValidationException.class,
        () ->
            new EpicTask(
                UUID.randomUUID(),
                SHORT_TITLE, // Invalid title
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                Set.of(),
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
            new EpicTask(
                UUID.randomUUID(),
                VALID_TITLE,
                SHORT_DESCRIPTION, // Invalid description
                TaskStatus.NEW,
                Set.of(),
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
            new EpicTask(
                null, // null id
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                Set.of(),
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
            new EpicTask(
                UUID.randomUUID(),
                null, // null title
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                Set.of(),
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
            new EpicTask(
                UUID.randomUUID(),
                VALID_TITLE,
                null, // null description
                TaskStatus.NEW,
                Set.of(),
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
            new EpicTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                null, // null status
                Set.of(),
                CREATION_TIME,
                UPDATE_TIME,
                null,
                null));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when subtaskIds set is null")
  void constructor_NullSubtaskIds_ThrowsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () ->
            new EpicTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                null, // null subtaskIds set
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
            new EpicTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                Set.of(),
                null, // null creationDateTime
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
            new EpicTask(
                UUID.randomUUID(),
                VALID_TITLE,
                VALID_DESCRIPTION,
                TaskStatus.NEW,
                Set.of(),
                CREATION_TIME,
                null, // null updateDateTime
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
                new EpicTask(
                    UUID.randomUUID(),
                    VALID_TITLE,
                    VALID_DESCRIPTION,
                    TaskStatus.NEW,
                    Set.of(),
                    creation,
                    updateBeforeCreation, // update time before creation time
                    null,
                    null));
    assertTrue(
        exception.getMessage().contains("The update date can't be before creation date."),
        "Exception message should indicate update date issue.");
  }

  @Test
  @DisplayName("getSubtaskIds should return an unmodifiable copy of subtask IDs")
  void getSubtaskIds_ReturnsUnmodifiableCopy() throws ValidationException {
    Set<UUID> initialSubtaskIds =
        Stream.of(UUID.randomUUID(), UUID.randomUUID()).collect(Collectors.toSet());
    EpicTask epic =
        new EpicTask(
            UUID.randomUUID(),
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.IN_PROGRESS,
            initialSubtaskIds,
            CREATION_TIME,
            UPDATE_TIME,
            null,
            null);

    Set<UUID> retrievedSubtaskIds = epic.getSubtaskIds();
    assertEquals(initialSubtaskIds.size(), retrievedSubtaskIds.size());
    assertTrue(retrievedSubtaskIds.containsAll(initialSubtaskIds));

    assertThrows(
        UnsupportedOperationException.class,
        () -> retrievedSubtaskIds.add(UUID.randomUUID()),
        "Should not be able to modify the returned set of subtask IDs");
    assertEquals(
        initialSubtaskIds.size(),
        epic.getSubtaskIds().size(),
        "Original subtask IDs in EpicTask should remain unchanged after attempt to modify copy");
  }

  @Test
  @DisplayName("toString should return a non-empty string representation")
  void toString_ReturnsNonEmptyString() throws ValidationException {
    EpicTask epic =
        new EpicTask(
            VALID_ID,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            VALID_SUBTASK_IDS,
            CREATION_TIME,
            UPDATE_TIME,
            START_TIME,
            DURATION);
    String str = epic.toString();
    assertNotNull(str);
    assertFalse(str.isEmpty());
    assertTrue(str.contains(VALID_ID.toString()));
    assertTrue(str.contains(VALID_TITLE));
    VALID_SUBTASK_IDS.forEach(
        subId -> assertTrue(str.contains(subId.toString()), "toString should contain subtask ID"));
  }
}
