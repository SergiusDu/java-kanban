package com.tasktracker.task.manager;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.HistoryRepository;
import com.tasktracker.task.store.InMemoryHistoryRepository;
import com.tasktracker.task.store.InMemoryTaskRepository;
import com.tasktracker.task.store.TaskRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link InMemoryHistoryManager} class, which ensures proper functionality of
 * the history management system, including maintaining a fixed size and handling edge cases such as
 * task reordering, task removal, and history constraints.
 */
class InMemoryHistoryManagerTest {

  private static final int HISTORY_LIMIT = 3;

  // Titles and descriptions must be strictly longer than 10 characters
  private static final String VALID_TITLE = "ValidTaskTitle"; // 14 chars
  private static final String VALID_DESCRIPTION = "ValidTaskDescr"; // 14 chars

  private TaskRepository taskRepository;
  private HistoryRepository historyRepository;
  private InMemoryHistoryManager historyManager;

  @BeforeEach
  void setUp() {
    taskRepository = new InMemoryTaskRepository();
    historyRepository = new InMemoryHistoryRepository();
    historyManager = new InMemoryHistoryManager(taskRepository, historyRepository, HISTORY_LIMIT);
  }

  @Test
  @DisplayName("History should be empty initially")
  void testInitialHistoryIsEmpty() {
    Collection<Task> history = historyManager.getHistory();
    assertTrue(history.isEmpty(), "History should be empty before any task access");
  }

  @Test
  @DisplayName("Adding a task ID not in TaskRepository should not appear in history")
  void testAddNonExistentTaskId() {
    boolean added = historyManager.add(999);
    Collection<Task> history = historyManager.getHistory();

    assertTrue(added, "HistoryManager should attempt to add the entry");
    assertTrue(history.isEmpty(), "Non-existent tasks should not appear in the final history");
  }

  @Test
  @DisplayName("Adding a valid task ID should appear in history")
  void testAddValidTaskId() {
    int taskId = taskRepository.generateId();
    RegularTask task =
        new RegularTask(
            taskId,
            VALID_TITLE, // > 10 characters
            VALID_DESCRIPTION, // > 10 characters
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    taskRepository.addTask(task);

    boolean added = historyManager.add(taskId);
    Collection<Task> history = historyManager.getHistory();

    assertTrue(added, "HistoryManager should return true after successfully adding a TaskView");
    assertEquals(1, history.size(), "History should contain exactly one task");
    assertEquals(
        taskId, history.iterator().next().getId(), "History should contain the correct task ID");
  }

  @Test
  @DisplayName("History limit should remove the oldest item when exceeded")
  void testHistoryLimitRemovesOldestItem() {
    int firstId =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskOneIsHere",
                    "DescOneIsLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();
    int secondId =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskTwoIsHere",
                    "DescTwoIsLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();
    int thirdId =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskThreeIsHere",
                    "DescThreeLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();
    int fourthId =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskFourIsHere",
                    "DescFourIsLg",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();

    historyManager.add(firstId);
    historyManager.add(secondId);
    historyManager.add(thirdId);

    Collection<Task> historyBefore = historyManager.getHistory();
    assertEquals(HISTORY_LIMIT, historyBefore.size(), "History should be at its limit");

    // Exceed the limit
    historyManager.add(fourthId);
    Collection<Task> historyAfter = historyManager.getHistory();

    assertEquals(HISTORY_LIMIT, historyAfter.size(), "History should still have only 3 tasks");
    boolean containsFirst = historyAfter.stream().anyMatch(t -> t.getId() == firstId);
    boolean containsFourth = historyAfter.stream().anyMatch(t -> t.getId() == fourthId);

    assertFalse(containsFirst, "Oldest task should have been removed");
    assertTrue(containsFourth, "Newest task should appear in the history");
  }

  @Test
  @DisplayName("HistoryManager should only return tasks that still exist in TaskRepository")
  void testHistoryExcludesRemovedTasks() {
    int existingTaskId = taskRepository.generateId();
    RegularTask existingTask =
        new RegularTask(
            existingTaskId,
            "TaskExistsLong",
            "DescExistsLong",
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    taskRepository.addTask(existingTask);

    int removedTaskId = taskRepository.generateId();
    RegularTask removedTask =
        new RegularTask(
            removedTaskId,
            "TaskRemoveLon",
            "RemoveDescLon",
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    taskRepository.addTask(removedTask);

    historyManager.add(existingTaskId);
    historyManager.add(removedTaskId);
    assertEquals(
        2, historyManager.getHistory().size(), "History should contain two tasks initially");

    taskRepository.removeTaskById(removedTaskId);
    Collection<Task> updatedHistory = historyManager.getHistory();

    assertEquals(1, updatedHistory.size(), "History should only contain the existing task");
    Optional<Task> maybeRemoved =
        updatedHistory.stream().filter(t -> t.getId() == removedTaskId).findFirst();
    assertFalse(maybeRemoved.isPresent(), "Removed task should not be in the history");
    assertTrue(
        updatedHistory.stream().anyMatch(t -> t.getId() == existingTaskId),
        "Existing task should remain");
  }

  @Test
  @DisplayName("Ensures pollFirst() is used if limit is reached, removing the earliest item")
  void testPollFirstIsUsedWhenLimitReached() {
    int taskId1 =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskOneIsLong",
                    "DescOneIsLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();
    int taskId2 =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskTwoIsLong",
                    "DescTwoIsLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();
    int taskId3 =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskThrIsLong",
                    "DescThrIsLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();
    int taskId4 =
        taskRepository
            .addTask(
                new RegularTask(
                    taskRepository.generateId(),
                    "TaskForIsLong",
                    "DescForIsLong",
                    TaskStatus.NEW,
                    LocalDateTime.now(),
                    LocalDateTime.now()))
            .getId();

    historyManager.add(taskId1); // [1]
    historyManager.add(taskId2); // [1, 2]
    historyManager.add(taskId3); // [1, 2, 3]

    historyManager.add(taskId4); // expected [2, 3, 4]
    Collection<Task> history = historyManager.getHistory();

    assertEquals(HISTORY_LIMIT, history.size(), "History size should remain at the limit");
    boolean hasTask1 = history.stream().anyMatch(t -> t.getId() == taskId1);
    boolean hasTask4 = history.stream().anyMatch(t -> t.getId() == taskId4);

    assertFalse(hasTask1, "Task1 should be removed (oldest)");
    assertTrue(hasTask4, "Task4 should be present (newest)");
  }

  @Test
  @DisplayName("getHistory() should not include tasks that never existed in TaskRepository")
  void testGetHistoryWithNeverExistedTask() {
    historyManager.add(-123);
    historyManager.add(-999);

    Collection<Task> history = historyManager.getHistory();
    assertTrue(history.isEmpty(), "History should not contain tasks that never existed");
  }

  @Test
  @DisplayName("Accessing a task should add it to the history")
  void testAddTaskToHistoryOnAccess() {
    int taskId = taskRepository.generateId();
    RegularTask task =
        new RegularTask(
            taskId,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    taskRepository.addTask(task);

    historyManager.add(taskId);
    Collection<Task> history = historyManager.getHistory();

    assertEquals(1, history.size(), "History should contain exactly one task");
    assertTrue(
        history.stream().anyMatch(t -> t.getId() == taskId),
        "History should contain the accessed task");
  }

  @Test
  @DisplayName("History should maintain correct order of task access")
  void testHistoryAccessOrder() {
    int taskId1 = taskRepository.generateId();
    int taskId2 = taskRepository.generateId();
    int taskId3 = taskRepository.generateId();

    taskRepository.addTask(
        new RegularTask(
            taskId1,
            "FirstAccessedX",
            "FirstAccessedY",
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now()));
    taskRepository.addTask(
        new RegularTask(
            taskId2,
            "SecondAccessd",
            "SecondAccessY",
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now()));
    taskRepository.addTask(
        new RegularTask(
            taskId3,
            "ThirdAccessed",
            "ThirdAccessedX",
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now()));

    historyManager.add(taskId1); // [1]
    historyManager.add(taskId2); // [1, 2]
    historyManager.add(taskId3); // [1, 2, 3]

    Collection<Task> history = historyManager.getHistory();
    assertEquals(HISTORY_LIMIT, history.size(), "Should contain three tasks");

    List<Task> historyList = history.stream().toList();
    assertEquals(taskId1, historyList.get(0).getId(), "First task in history should be taskId1");
    assertEquals(taskId2, historyList.get(1).getId(), "Second task in history should be taskId2");
    assertEquals(taskId3, historyList.get(2).getId(), "Third task in history should be taskId3");
  }

  @Test
  @DisplayName("History should not exceed the defined limit even with rapid additions")
  void testHistoryDoesNotExceedLimitWithRapidAdditions() {
    for (int i = 1; i <= 5; i++) {
      int taskId = taskRepository.generateId();
      // Ensures >10 characters for both title and description
      String title = String.format("RapidAddTask%02d", i); // e.g., "RapidAddTask01"
      String description = String.format("RapidDescTest%02d", i); // e.g., "RapidDescTest01"
      taskRepository.addTask(
          new RegularTask(
              taskId,
              title,
              description,
              TaskStatus.NEW,
              LocalDateTime.now(),
              LocalDateTime.now()));
      historyManager.add(taskId);
    }

    Collection<Task> history = historyManager.getHistory();
    assertEquals(HISTORY_LIMIT, history.size(), "History should not exceed the defined limit");

    // The last three tasks should be present
    for (int i = 3; i <= 5; i++) {
      int expectedTaskNumber = i;
      String expectedTitle = String.format("RapidAddTask%02d", expectedTaskNumber);
      boolean exists = history.stream().anyMatch(t -> t.getTitle().equals(expectedTitle));
      assertTrue(exists, "History should contain " + expectedTitle);
    }
    // The first two tasks should have been removed
    for (int i = 1; i <= 2; i++) {
      int expectedTaskNumber = i;
      String expectedTitle = String.format("RapidAddTask%02d", expectedTaskNumber);
      boolean exists = history.stream().anyMatch(t -> t.getTitle().equals(expectedTitle));
      assertFalse(exists, "History should not contain " + expectedTitle);
    }
  }
}
