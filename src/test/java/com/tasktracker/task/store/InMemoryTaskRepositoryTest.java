package com.tasktracker.task.store;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.Task;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive JUnit5 tests for {@link InMemoryTaskRepository}. Demonstrates correct usage where
 * IDs are generated internally. Tasks have title/description > 10 chars and valid {@link
 * TaskStatus}.
 */
class InMemoryTaskRepositoryTest {

  private static final String VALID_TITLE = "ValidLongTitle"; // length > 10
  private static final String VALID_DESCRIPTION = "ValidLongDesc"; // length > 10

  private InMemoryTaskRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryTaskRepository();
  }

  /**
   * Helper method to create and add a task without manually reserving an ID. The repository assigns
   * the ID during addTask(), and we retrieve it from the returned Task.
   */
  private RegularTask createAndStoreTask(String title, String description, TaskStatus status) {
    // Pass an arbitrary placeholder ID (e.g., -1), or 0. The repository won't rely on it
    // if you manage ID generation within addTask() or via generateId() inline.
    // However, the current InMemoryTaskRepository requires you to call generateId().
    // So we do that inline before constructing the task:
    int newId = repository.generateId();
    RegularTask task =
        new RegularTask(
            newId, title, description, status, LocalDateTime.now(), LocalDateTime.now());
    return repository.addTask(task);
  }

  @Test
  @DisplayName("Should successfully add a valid task")
  void testAddTaskValid() {
    RegularTask saved = createAndStoreTask(VALID_TITLE, VALID_DESCRIPTION, TaskStatus.NEW);
    int assignedId = saved.getId();

    assertNotNull(saved, "Returned task from addTask() should not be null");
    assertEquals(VALID_TITLE, saved.getTitle(), "Title should match input");
    assertEquals(VALID_DESCRIPTION, saved.getDescription(), "Description should match input");

    // Verify the task is actually stored
    Optional<Task> retrieved = repository.getTaskById(assignedId);
    assertTrue(retrieved.isPresent(), "Repository should contain the new task");
    assertEquals(VALID_TITLE, retrieved.get().getTitle(), "Stored task's title should match");
  }

  @Test
  @DisplayName("Should throw NullPointerException when adding a null task")
  void testAddTaskNullThrowsException() {
    assertThrows(NullPointerException.class, () -> repository.addTask(null));
  }

  @Test
  @DisplayName("Should update an existing task successfully")
  void testUpdateTaskValid() {
    RegularTask saved = createAndStoreTask(VALID_TITLE, VALID_DESCRIPTION, TaskStatus.NEW);
    int assignedId = saved.getId();

    // Create an updated copy with the same ID
    RegularTask updatedTask =
        new RegularTask(
            assignedId,
            "UpdatedTaskTitle", // length > 10
            "UpdatedTaskDescX", // length > 10
            TaskStatus.IN_PROGRESS,
            saved.getCreationDate(),
            LocalDateTime.now());

    Task result = repository.updateTask(updatedTask);
    assertEquals(assignedId, result.getId(), "Updated task should retain the same ID");
    assertEquals("UpdatedTaskTitle", result.getTitle(), "Title should be updated");
    assertEquals("UpdatedTaskDescX", result.getDescription(), "Description should be updated");
    assertEquals(TaskStatus.IN_PROGRESS, result.getStatus(), "Status should be updated");

    // Ensure repository reflects the changes
    Optional<Task> stored = repository.getTaskById(assignedId);
    assertTrue(stored.isPresent(), "Task should still exist");
    assertEquals(
        "UpdatedTaskTitle", stored.get().getTitle(), "Repository should have updated title");
  }

  @Test
  @DisplayName("Should throw NullPointerException when updating a null task")
  void testUpdateTaskNullThrowsException() {
    assertThrows(NullPointerException.class, () -> repository.updateTask(null));
  }

  @Test
  @DisplayName("Should throw NoSuchElementException when updating a non-existent task")
  void testUpdateNonExistentTaskThrowsException() {
    // We do not store this task, so it does not exist in the repository
    RegularTask nonExistent =
        new RegularTask(
            999,
            "NonExistentTitl", // length > 10
            "NonExistentDescr", // length > 10
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());

    assertThrows(
        NoSuchElementException.class,
        () -> repository.updateTask(nonExistent),
        "Updating a non-existent task should throw NoSuchElementException");
  }

  @Test
  @DisplayName("Should retrieve all tasks in the repository")
  void testGetAllTasks() {
    createAndStoreTask("TitleAllTasksA", "DescrAllTasksA", TaskStatus.NEW);
    createAndStoreTask("TitleAllTasksB", "DescrAllTasksB", TaskStatus.NEW);

    Collection<Task> tasks = repository.getAllTasks();
    assertEquals(2, tasks.size(), "Should retrieve exactly 2 tasks");
  }

  @Test
  @DisplayName("Should retrieve a task by existing ID")
  void testGetTaskByIdValid() {
    RegularTask saved = createAndStoreTask(VALID_TITLE, VALID_DESCRIPTION, TaskStatus.NEW);
    int assignedId = saved.getId();

    Optional<Task> result = repository.getTaskById(assignedId);
    assertTrue(result.isPresent(), "Task with valid ID should be found");
    assertEquals(assignedId, result.get().getId(), "Retrieved task ID should match");
    assertEquals(VALID_TITLE, result.get().getTitle(), "Retrieved task title should match");
  }

  @Test
  @DisplayName("Should return empty Optional when retrieving a non-existent ID")
  void testGetTaskByIdNonExistent() {
    Optional<Task> result = repository.getTaskById(999);
    assertTrue(result.isEmpty(), "Should return empty if the task does not exist");
  }

  @Test
  @DisplayName("Should remove a task by ID and return it")
  void testRemoveTaskValid() {
    RegularTask saved = createAndStoreTask("RemoveTaskXXXX", "RemoveDescrXXXX", TaskStatus.NEW);
    int assignedId = saved.getId();

    Optional<Task> removed = repository.removeTask(assignedId);
    assertTrue(removed.isPresent(), "Should return the removed task");
    assertEquals(assignedId, removed.get().getId(), "Removed task ID should match");
    assertTrue(
        repository.getTaskById(assignedId).isEmpty(), "Task should no longer exist in repository");
  }

  @Test
  @DisplayName("Should throw NoSuchElementException when removing a non-existent ID")
  void testRemoveTaskNonExistent() {
    assertThrows(
        NoSuchElementException.class,
        () -> repository.removeTask(12345),
        "Removing a non-existent ID should throw NoSuchElementException");
  }

  @Test
  @DisplayName("Should find tasks matching a given predicate")
  void testFindTasksMatching() {
    RegularTask t1 = createAndStoreTask("SearchTitleABC", "SearchDescrABC", TaskStatus.NEW);
    RegularTask t2 = createAndStoreTask("UnrelatedTaskX", "UnrelatedDescrX", TaskStatus.NEW);
    RegularTask t3 = createAndStoreTask("SearchTitleXYZ", "SearchDescrXYZ", TaskStatus.NEW);

    Predicate<Task> matchingTitle = t -> t.getTitle().contains("SearchTitle");
    Collection<Task> foundTasks = repository.findTasksMatching(matchingTitle);

    assertEquals(2, foundTasks.size(), "Should find 2 tasks with matching titles");
    boolean foundT1 = foundTasks.stream().anyMatch(task -> task.getId() == t1.getId());
    boolean foundT3 = foundTasks.stream().anyMatch(task -> task.getId() == t3.getId());
    assertTrue(foundT1 && foundT3, "Should include t1 and t3 in the results");
    assertFalse(
        foundTasks.stream().anyMatch(task -> task.getId() == t2.getId()), "t2 should not match");
  }

  @Test
  @DisplayName("Should remove tasks matching a given predicate")
  void testRemoveMatchingTasks() {
    RegularTask toRemove = createAndStoreTask("RemoveTitleAAA", "RemoveDescAAAA", TaskStatus.NEW);
    RegularTask toKeep = createAndStoreTask("KeepTitleBBBB", "KeepDescBBBBB", TaskStatus.NEW);

    boolean removed = repository.removeMatchingTasks(t -> t.getTitle().startsWith("Remove"));
    assertTrue(removed, "Should remove at least one task matching the predicate");
    assertTrue(
        repository.getTaskById(toRemove.getId()).isEmpty(), "Removed task should no longer exist");
    assertTrue(
        repository.getTaskById(toKeep.getId()).isPresent(), "Non-matching task should remain");
  }

  @Test
  @DisplayName("Should clear all tasks from the repository")
  void testClearAllTasks() {
    createAndStoreTask("ClearTitleOk1", "ClearDescOkay1", TaskStatus.NEW);
    createAndStoreTask("ClearTitleOk2", "ClearDescOkay2", TaskStatus.NEW);

    assertFalse(repository.getAllTasks().isEmpty(), "Should have tasks before clearing");

    repository.clearAllTasks();
    assertTrue(repository.getAllTasks().isEmpty(), "All tasks should be cleared");
  }

  @Test
  @DisplayName("Should generate IDs sequentially and store tasks accordingly")
  void testGenerateIdAndStorage() {
    // First add
    RegularTask first = createAndStoreTask("FirstGenIDTask", "FirstGenIDDesc", TaskStatus.NEW);
    int firstAssignedId = first.getId();
    // Next add
    RegularTask second = createAndStoreTask("SecondGenIDTsk", "SecondGenIDDsc", TaskStatus.NEW);
    int secondAssignedId = second.getId();

    assertTrue(secondAssignedId > firstAssignedId, "Second ID should be greater than first ID");

    // Verify both are stored
    assertTrue(repository.getTaskById(firstAssignedId).isPresent(), "First task should exist");
    assertTrue(repository.getTaskById(secondAssignedId).isPresent(), "Second task should exist");
  }
}
