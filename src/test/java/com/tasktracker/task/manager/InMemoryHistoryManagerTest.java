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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryHistoryManagerTest {

  private static final String VALID_TITLE = "ValidTaskTitleX";
  private static final String VALID_DESCRIPTION = "ValidTaskDescrX";
  private static final int HISTORY_LIMIT = 3;

  private HistoryRepository historyRepository;
  private TaskRepository taskRepository;
  private InMemoryHistoryManager historyManager;

  @BeforeEach
  void init() {
    historyRepository = new InMemoryHistoryRepository();
    taskRepository = new InMemoryTaskRepository();
    historyManager = new InMemoryHistoryManager(historyRepository, HISTORY_LIMIT);
  }

  @Test
  @DisplayName("Should throw NullPointerException when historyStore is null in constructor")
  void shouldThrowExceptionForNullHistoryStore() {
    assertThrows(NullPointerException.class, () -> new InMemoryHistoryManager(null, HISTORY_LIMIT));
  }

  @Test
  @DisplayName("Should throw IllegalArgumentException when history limit is below 1")
  void shouldThrowExceptionForInvalidHistoryLimit() {
    assertThrows(
        IllegalArgumentException.class, () -> new InMemoryHistoryManager(historyRepository, 0));
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
    assertThrows(NullPointerException.class, () -> historyManager.add(null));
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
    boolean added = historyManager.add(task);
    Collection<TaskView> history = historyManager.getHistory();
    assertTrue(added);
    assertEquals(1, history.size());
    TaskView view = history.iterator().next();
    assertEquals(generatedId, view.getTaskId());
  }

  @Test
  @DisplayName("Should remove the oldest entry when the limit is exceeded")
  void shouldRemoveOldestWhenLimitIsReached() {
    Task t1 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "TaskOneTitleZ",
                "TaskOneDescrZ",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t2 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "TaskTwoTitleZ",
                "TaskTwoDescrZ",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t3 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "TaskThreeTTLZ",
                "TaskThreeDSCZ",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    Task t4 =
        taskRepository.addTask(
            new RegularTask(
                taskRepository.generateId(),
                "TaskFourTitle",
                "TaskFourDescrip",
                TaskStatus.NEW,
                LocalDateTime.now(),
                LocalDateTime.now()));
    historyManager.add(t1);
    historyManager.add(t2);
    historyManager.add(t3);
    assertEquals(HISTORY_LIMIT, historyManager.getHistory().size());
    historyManager.add(t4);
    Collection<TaskView> history = historyManager.getHistory();
    assertEquals(HISTORY_LIMIT, history.size());
    assertFalse(history.stream().anyMatch(v -> v.getTaskId() == t1.getId()));
    assertTrue(history.stream().anyMatch(v -> v.getTaskId() == t4.getId()));
  }

  @Test
  @DisplayName("Should keep adding tasks rapidly without exceeding the limit")
  void shouldNotExceedLimitOnRapidAdd() {
    for (int i = 0; i < 5; i++) {
      Task task =
          new RegularTask(
              taskRepository.generateId(),
              "RapidAddTitle" + i,
              "RapidAddDescrip" + i,
              TaskStatus.NEW,
              LocalDateTime.now(),
              LocalDateTime.now());
      taskRepository.addTask(task);
      historyManager.add(task);
    }
    Collection<TaskView> history = historyManager.getHistory();
    assertEquals(HISTORY_LIMIT, history.size());
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
    historyManager.add(t1);
    historyManager.add(t2);
    historyManager.add(t3);
    TaskView[] items = historyManager.getHistory().toArray(TaskView[]::new);
    assertEquals(t1.getId(), items[0].getTaskId());
    assertEquals(t2.getId(), items[1].getTaskId());
    assertEquals(t3.getId(), items[2].getTaskId());
  }
}
