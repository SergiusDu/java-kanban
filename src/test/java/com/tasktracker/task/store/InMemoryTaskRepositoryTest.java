package com.tasktracker.task.store;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException; // Added for clarity
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryTaskRepositoryTest {

  private static final String VALID_TITLE_PREFIX = "Valid Title ";
  private static final String VALID_DESCRIPTION_PREFIX = "Valid Description ";
  private static final LocalDateTime DEFAULT_CREATION_TIME = LocalDateTime.now().minusHours(2);
  private static final LocalDateTime DEFAULT_UPDATE_TIME = LocalDateTime.now().minusHours(1);
  private static final LocalDateTime DEFAULT_START_TIME = LocalDateTime.now().plusDays(1);
  private static final Duration DEFAULT_DURATION = Duration.ofHours(2);

  private InMemoryTaskRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTaskRepository();
  }

  private RegularTask createRegularTask(
      UUID id, String titleSuffix, TaskStatus status, LocalDateTime startTime, Duration duration) {
    try {
      return new RegularTask(
          id,
          VALID_TITLE_PREFIX + titleSuffix,
          VALID_DESCRIPTION_PREFIX + titleSuffix,
          status,
          DEFAULT_CREATION_TIME,
          DEFAULT_UPDATE_TIME,
          startTime,
          duration);
    } catch (Exception e) {
      fail("Task creation failed: " + e.getMessage());
      return null; // Should not reach here
    }
  }

  private RegularTask createAndAddTask(
      String titleSuffix, TaskStatus status, LocalDateTime startTime, Duration duration) {
    UUID id = UUID.randomUUID();
    RegularTask task = createRegularTask(id, titleSuffix, status, startTime, duration);
    assertNotNull(task, "Helper method createRegularTask returned null");
    repository.addTask(task);
    return task;
  }

  @Test
  @DisplayName("addTask: Should successfully add a valid task")
  void addTask_ValidTask_ShouldBeStored() {
    RegularTask task = createAndAddTask("AddValid", TaskStatus.NEW, null, null);
    Optional<Task> retrieved = repository.getTaskById(task.getId());
    assertTrue(retrieved.isPresent(), "Repository should contain the new task");
    assertEquals(task.getId(), retrieved.get().getId(), "Stored task's ID should match");
    assertEquals(task.getTitle(), retrieved.get().getTitle(), "Stored task's title should match");
  }

  @Test
  @DisplayName("addTask: Should throw NullPointerException when adding a null task")
  void addTask_NullTask_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.addTask(null));
  }

  @Test
  @DisplayName(
      "addTask: Should throw IllegalArgumentException when adding a task with an existing ID")
  void addTask_DuplicateId_ShouldThrowIllegalArgumentException() {
    RegularTask task1 = createAndAddTask("DuplicateId1", TaskStatus.NEW, null, null);
    RegularTask task2WithSameId =
        createRegularTask(task1.getId(), "DuplicateId2", TaskStatus.IN_PROGRESS, null, null);

    assertThrows(IllegalArgumentException.class, () -> repository.addTask(task2WithSameId));
  }

  @Test
  @DisplayName("updateTask: Should update an existing task successfully and return the old task")
  void updateTask_ExistingTask_ShouldUpdateAndReturnOld()
      throws TaskNotFoundException, ValidationException {
    RegularTask originalTask =
        createAndAddTask("UpdateOriginal", TaskStatus.NEW, DEFAULT_START_TIME, DEFAULT_DURATION);
    LocalDateTime newUpdateTime = LocalDateTime.now();
    RegularTask updatedVersion =
        new RegularTask(
            originalTask.getId(),
            VALID_TITLE_PREFIX + "UpdateModified",
            VALID_DESCRIPTION_PREFIX + "UpdateModified",
            TaskStatus.IN_PROGRESS,
            originalTask.getCreationDate(), // Creation date should not change
            newUpdateTime,
            DEFAULT_START_TIME.plusHours(1),
            DEFAULT_DURATION.plusMinutes(30));

    Task oldTaskReturned = repository.updateTask(updatedVersion);

    assertNotNull(oldTaskReturned, "updateTask should return the old task version");
    assertEquals(originalTask.getId(), oldTaskReturned.getId());
    assertEquals(originalTask.getTitle(), oldTaskReturned.getTitle());
    assertEquals(originalTask.getDescription(), oldTaskReturned.getDescription());
    assertEquals(originalTask.getStatus(), oldTaskReturned.getStatus());
    assertEquals(originalTask.getStartTime(), oldTaskReturned.getStartTime());
    assertEquals(originalTask.getDuration(), oldTaskReturned.getDuration());

    Optional<Task> storedAfterUpdate = repository.getTaskById(originalTask.getId());
    assertTrue(storedAfterUpdate.isPresent(), "Task should still exist after update");
    Task currentTaskInRepo = storedAfterUpdate.get();
    assertEquals(updatedVersion.getTitle(), currentTaskInRepo.getTitle());
    assertEquals(updatedVersion.getDescription(), currentTaskInRepo.getDescription());
    assertEquals(updatedVersion.getStatus(), currentTaskInRepo.getStatus());
    assertEquals(updatedVersion.getStartTime(), currentTaskInRepo.getStartTime());
    assertEquals(updatedVersion.getDuration(), currentTaskInRepo.getDuration());
    assertEquals(newUpdateTime, currentTaskInRepo.getUpdateDate());
  }

  @Test
  @DisplayName("updateTask: Should throw NullPointerException when updating with a null task")
  void updateTask_NullTask_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.updateTask(null));
  }

  @Test
  @DisplayName("updateTask: Should throw TaskNotFoundException when updating a non-existent task")
  void updateTask_NonExistentTask_ShouldThrowTaskNotFoundException() {
    RegularTask nonExistentTask =
        createRegularTask(UUID.randomUUID(), "UpdateNonExistent", TaskStatus.NEW, null, null);
    assertThrows(TaskNotFoundException.class, () -> repository.updateTask(nonExistentTask));
  }

  @Test
  @DisplayName("getAllTasks: Should retrieve all tasks in the repository")
  void getAllTasks_MultipleTasks_ShouldReturnAll() {
    createAndAddTask("GetAll1", TaskStatus.NEW, null, null);
    createAndAddTask("GetAll2", TaskStatus.IN_PROGRESS, DEFAULT_START_TIME, DEFAULT_DURATION);
    Collection<Task> tasks = repository.getAllTasks();
    assertEquals(2, tasks.size(), "Should retrieve exactly 2 tasks");
  }

  @Test
  @DisplayName("getAllTasks: Should return an empty collection if repository is empty")
  void getAllTasks_EmptyRepository_ShouldReturnEmptyCollection() {
    Collection<Task> tasks = repository.getAllTasks();
    assertTrue(tasks.isEmpty(), "Should return an empty collection for an empty repository");
  }

  @Test
  @DisplayName("getTaskById: Should retrieve a task by existing ID")
  void getTaskById_ExistingId_ShouldReturnTask() {
    RegularTask task = createAndAddTask("GetByIdValid", TaskStatus.DONE, null, null);
    Optional<Task> result = repository.getTaskById(task.getId());
    assertTrue(result.isPresent(), "Task with valid ID should be found");
    assertEquals(task.getId(), result.get().getId(), "Retrieved task ID should match");
  }

  @Test
  @DisplayName("getTaskById: Should return empty Optional when retrieving a non-existent ID")
  void getTaskById_NonExistentId_ShouldReturnEmptyOptional() {
    Optional<Task> result = repository.getTaskById(UUID.randomUUID());
    assertTrue(result.isEmpty(), "Should return empty if the task does not exist");
  }

  @Test
  @DisplayName("getTaskById: Should throw NullPointerException if ID is null")
  void getTaskById_NullId_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.getTaskById(null));
  }

  @Test
  @DisplayName("removeTask: Should remove a task by ID and return it")
  void removeTask_ExistingId_ShouldRemoveAndReturnTask() {
    RegularTask task = createAndAddTask("RemoveValid", TaskStatus.NEW, null, null);
    Optional<Task> removed = repository.removeTask(task.getId());
    assertTrue(removed.isPresent(), "Should return the removed task");
    assertEquals(task.getId(), removed.get().getId(), "Removed task ID should match");
    assertTrue(
        repository.getTaskById(task.getId()).isEmpty(),
        "Task should no longer exist in repository");
  }

  @Test
  @DisplayName("removeTask: Should return empty Optional when removing a non-existent ID")
  void removeTask_NonExistentId_ShouldReturnEmptyOptional() {
    Optional<Task> removed = repository.removeTask(UUID.randomUUID());
    assertTrue(removed.isEmpty(), "Removing a non-existent ID should return an empty Optional");
  }

  @Test
  @DisplayName("removeTask: Should throw NullPointerException if ID is null")
  void removeTask_NullId_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.removeTask(null));
  }

  @Test
  @DisplayName("findTasksMatching: Should find tasks matching a given predicate")
  void findTasksMatching_PredicateMatches_ShouldReturnMatchingTasks() {
    RegularTask task1 =
        createAndAddTask("FindMatchABC", TaskStatus.NEW, DEFAULT_START_TIME, DEFAULT_DURATION);
    createAndAddTask("FindUnrelated", TaskStatus.IN_PROGRESS, null, null); // task2
    RegularTask task3 = createAndAddTask("FindMatchXYZ", TaskStatus.DONE, null, null);

    Predicate<Task> matchingTitle = t -> t.getTitle().contains("FindMatch");
    Collection<Task> foundTasks = repository.findTasksMatching(matchingTitle);

    assertEquals(2, foundTasks.size(), "Should find 2 tasks with matching titles");
    assertTrue(
        foundTasks.stream().anyMatch(t -> t.getId().equals(task1.getId())), "Should include task1");
    assertTrue(
        foundTasks.stream().anyMatch(t -> t.getId().equals(task3.getId())), "Should include task3");
  }

  @Test
  @DisplayName("findTasksMatching: Should return empty collection if no tasks match predicate")
  void findTasksMatching_PredicateNoMatch_ShouldReturnEmptyCollection() {
    createAndAddTask("NoMatch1", TaskStatus.NEW, null, null);
    Predicate<Task> nonMatchingPredicate = t -> t.getTitle().contains("ThisWontMatch");
    Collection<Task> foundTasks = repository.findTasksMatching(nonMatchingPredicate);
    assertTrue(foundTasks.isEmpty());
  }

  @Test
  @DisplayName("findTasksMatching: Should throw NullPointerException if predicate is null")
  void findTasksMatching_NullPredicate_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.findTasksMatching(null));
  }

  @Test
  @DisplayName(
      "removeMatchingTasks: Should remove tasks matching a given predicate and return true")
  void removeMatchingTasks_PredicateMatches_ShouldRemoveAndReturnTrue() {
    RegularTask taskToRemove = createAndAddTask("RemoveMatch", TaskStatus.NEW, null, null);
    RegularTask taskToKeep = createAndAddTask("KeepThis", TaskStatus.IN_PROGRESS, null, null);

    Predicate<Task> matchingPredicate = t -> t.getTitle().endsWith("RemoveMatch");
    boolean removed = repository.removeMatchingTasks(matchingPredicate);

    assertTrue(removed, "Should return true as at least one task was removed");
    assertTrue(
        repository.getTaskById(taskToRemove.getId()).isEmpty(),
        "Matched task should no longer exist");
    assertTrue(
        repository.getTaskById(taskToKeep.getId()).isPresent(), "Non-matching task should remain");
  }

  @Test
  @DisplayName(
      "removeMatchingTasks: Should return false if no tasks match predicate and not remove any")
  void removeMatchingTasks_PredicateNoMatch_ShouldReturnFalseAndNotRemove() {
    RegularTask task1 = createAndAddTask("NoRemoveMatch1", TaskStatus.NEW, null, null);
    RegularTask task2 = createAndAddTask("NoRemoveMatch2", TaskStatus.DONE, null, null);

    Predicate<Task> nonMatchingPredicate = t -> t.getTitle().contains("ThisWontMatch");
    boolean removed = repository.removeMatchingTasks(nonMatchingPredicate);

    assertFalse(removed, "Should return false as no tasks were removed");
    assertEquals(2, repository.getAllTasks().size(), "No tasks should have been removed");
    assertTrue(repository.getTaskById(task1.getId()).isPresent());
    assertTrue(repository.getTaskById(task2.getId()).isPresent());
  }

  @Test
  @DisplayName("removeMatchingTasks: Should throw NullPointerException if predicate is null")
  void removeMatchingTasks_NullPredicate_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.removeMatchingTasks(null));
  }

  @Test
  @DisplayName("clearAllTasks: Should clear all tasks from the repository")
  void clearAllTasks_WithExistingTasks_ShouldEmptyRepository() {
    createAndAddTask("Clear1", TaskStatus.NEW, null, null);
    createAndAddTask("Clear2", TaskStatus.IN_PROGRESS, null, null);

    assertFalse(repository.getAllTasks().isEmpty(), "Repository should have tasks before clearing");
    repository.clearAllTasks();
    assertTrue(repository.getAllTasks().isEmpty(), "All tasks should be cleared");
  }

  @Test
  @DisplayName("clearAllTasks: Should do nothing if repository is already empty")
  void clearAllTasks_EmptyRepository_ShouldRemainEmpty() {
    assertTrue(repository.getAllTasks().isEmpty(), "Repository should be initially empty");
    repository.clearAllTasks();
    assertTrue(repository.getAllTasks().isEmpty(), "Repository should remain empty");
  }
}
