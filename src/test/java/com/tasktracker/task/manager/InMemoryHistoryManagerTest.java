package com.tasktracker.task.manager;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryRepository;
import com.tasktracker.task.store.InMemoryHistoryRepository;
import com.tasktracker.task.store.InMemoryTaskRepository;
import com.tasktracker.task.store.TaskRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryHistoryManagerTest {

  private static final String VALID_TITLE = "ValidTaskTitleX";
  private static final String VALID_DESCRIPTION = "ValidTaskDescrX";

  private TaskRepository taskRepository;
  private InMemoryHistoryManager historyManager;

  @BeforeEach
  void init() {
    HistoryRepository historyRepository = new InMemoryHistoryRepository();
    taskRepository = new InMemoryTaskRepository();
    historyManager = new InMemoryHistoryManager(historyRepository);
  }

  @Test
  @DisplayName("Should throw NullPointerException when historyStore is null in constructor")
  void shouldThrowExceptionForNullHistoryStore() {
    assertThrows(NullPointerException.class, () -> new InMemoryHistoryManager(null));
  }

  @Test
  @DisplayName("Should return an empty history at the start")
  void shouldReturnEmptyHistoryInitially() {
    Collection<TaskView> history = historyManager.getHistory();
    assertTrue(history.isEmpty());
  }

  @Test
  @DisplayName("Should throw NullPointerException when adding a null task")
  void shouldThrowExceptionWhenAddingNullTask() {
    assertThrows(NullPointerException.class, () -> historyManager.put(null));
  }

  @Test
  @DisplayName("Should add an existing task to history")
  void shouldAddExistingTask() {
    int generatedId = taskRepository.generateId();
    Task task =
        new RegularTask(
            generatedId,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    taskRepository.addTask(task);
    Optional<TaskView> added = historyManager.put(task);
    Collection<TaskView> history = historyManager.getHistory();
    assertNotNull(added);
    assertEquals(1, history.size());
    TaskView view = history.iterator().next();
    assertEquals(generatedId, view.getTaskId());
  }

  @Test
  @DisplayName("Should keep tasks in correct order of access")
  void shouldMaintainAccessOrder() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t2 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "SecondTaskTTL",
                "SecondTaskDSC",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t3 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "ThirdTaskTitle",
                "ThirdTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    historyManager.put(t2);
    historyManager.put(t3);
    TaskView[] items = historyManager.getHistory().toArray(TaskView[]::new);
    assertEquals(t1.getId(), items[0].getTaskId());
    assertEquals(t2.getId(), items[1].getTaskId());
    assertEquals(t3.getId(), items[2].getTaskId());
  }

  @Test
  @DisplayName("Should contain task history")
  void shouldDisplayTaskHistory() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t2 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "SecondTaskTTL",
                "SecondTaskDSC",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t3 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "ThirdTaskTitle",
                "ThirdTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    historyManager.put(t2);
    historyManager.put(t3);
    Collection<TaskView> taskHistory = historyManager.getHistory();
    assertEquals(3, taskHistory.size());
  }

  @Test
  @DisplayName("Should contain only unique tasks in history")
  void shouldDeisplayOnlyUniqueTasksInHistory() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t2 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "SecondTaskTTL",
                "SecondTaskDSC",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    historyManager.put(t1);
    historyManager.put(t2);
    Collection<TaskView> taskHistory = historyManager.getHistory();
    assertEquals(2, taskHistory.size());
  }

  @Test
  @DisplayName("Should add task in historyManager")
  void put() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    Collection<TaskView> taskHistory = historyManager.getHistory();
    assertEquals(1, taskHistory.size());
  }

  @DisplayName("Should remove TaskView by ID")
  @Test
  void shouldRemoveTaskViewById() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    historyManager.remove(t1.getId());
    assertEquals(0, historyManager.getHistory().size());
  }

  @DisplayName("Should return Optional Empty when task not removed")
  @Test
  void shouldReturnOptionalWhenTaskNotRemoved() {
    assertInstanceOf(Optional.class, historyManager.remove(0));
    assertTrue(historyManager.remove(0).isEmpty());
  }

  @DisplayName("Should return TaskView when task is removed from history")
  @Test
  void shouldReturnTaskViewWhenTaskRemovedFromHistory() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    Optional<TaskView> removedT1 = historyManager.remove(t1.getId());
    assertTrue(removedT1.isPresent());
    assertEquals(t1.getId(), removedT1.get().getTaskId());
  }

  @Test
  void shouldMaintainCorrectHistoryOrder() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t2 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t3 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "FirstTaskTitle",
                "FirstTaskDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.put(t1);
    historyManager.put(t2);
    historyManager.put(t3);
    historyManager.put(t2);
    Collection<TaskView> history = historyManager.getHistory();
    assertEquals(3, history.size());
    System.out.println(historyManager.getHistory());
  }
}
