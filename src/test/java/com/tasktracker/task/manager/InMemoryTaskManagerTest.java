package com.tasktracker.task.manager;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.*;
import com.tasktracker.task.store.InMemoryHistoryStore;
import com.tasktracker.task.store.InMemoryTaskRepository;
import com.tasktracker.task.store.TaskRepository;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** JUnit5 tests for TaskManagerImpl covering edge cases and business logic. */
public class InMemoryTaskManagerTest {

  // Valid test constants
  private static final String VALID_TITLE_PREFIX = "Valid Task Title ";
  private static final String VALID_DESCRIPTION_PREFIX =
      "Valid Task Description with enough characters ";
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
  private static final Duration DEFAULT_DURATION = Duration.ofHours(2);
  private static final LocalDateTime DEFAULT_START_TIME_2 = DEFAULT_START_TIME.plusDays(1);

  // Invalid test constants
  private static final String INVALID_SHORT_TITLE = "Short";
  private static final String INVALID_SHORT_DESCRIPTION = "ShortDesc";

  private TaskManager manager;
  private TaskRepository taskRepository;
  private HistoryManager historyManager;

  @BeforeEach
  void setUp() {
    taskRepository = new InMemoryTaskRepository();
    historyManager = new InMemoryHistoryManager(new InMemoryHistoryStore());
    manager = new TaskManagerImpl(taskRepository, historyManager);
  }

  // --- Helper Methods for DTO creation ---
  private RegularTaskCreationDTO createValidRegularTaskCreationDTO(String titleSuffix) {
    return new RegularTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix, VALID_DESCRIPTION_PREFIX + titleSuffix, null, null);
  }

  private RegularTaskCreationDTO createValidRegularTaskCreationDTOWithTime(
      String titleSuffix, LocalDateTime startTime, Duration duration) {
    return new RegularTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix,
        VALID_DESCRIPTION_PREFIX + titleSuffix,
        startTime,
        duration);
  }

  private EpicTaskCreationDTO createValidEpicTaskCreationDTO(String titleSuffix) {
    return new EpicTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix, VALID_DESCRIPTION_PREFIX + titleSuffix, null);
  }

  private EpicTaskCreationDTO createValidEpicTaskCreationDTOWithTime(
      String titleSuffix, LocalDateTime startTime) {
    return new EpicTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix, VALID_DESCRIPTION_PREFIX + titleSuffix, startTime);
  }

  private SubTaskCreationDTO createValidSubTaskCreationDTO(String titleSuffix, UUID epicId) {
    return new SubTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix,
        VALID_DESCRIPTION_PREFIX + titleSuffix,
        epicId,
        null,
        null);
  }

  private SubTaskCreationDTO createValidSubTaskCreationDTOWithTime(
      String titleSuffix, UUID epicId, LocalDateTime startTime, Duration duration) {
    return new SubTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix,
        VALID_DESCRIPTION_PREFIX + titleSuffix,
        epicId,
        startTime,
        duration);
  }

  // --- Helper Methods for Task Addition and Retrieval ---
  private RegularTask addAndRetrieveRegularTask(RegularTaskCreationDTO dto)
      throws ValidationException {
    Set<UUID> idsBefore =
        manager.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    manager.addTask(dto);
    return manager.getAllTasksByClass(RegularTask.class).stream()
        .filter(t -> !idsBefore.contains(t.getId()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Failed to retrieve added RegularTask with title: " + dto.title()));
  }

  private EpicTask addAndRetrieveEpicTask(EpicTaskCreationDTO dto) throws ValidationException {
    Set<UUID> idsBefore =
        manager.getAllTasksByClass(EpicTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    manager.addTask(dto);
    return manager.getAllTasksByClass(EpicTask.class).stream()
        .filter(t -> !idsBefore.contains(t.getId()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Failed to retrieve added EpicTask with title: " + dto.title()));
  }

  private SubTask addAndRetrieveSubTask(SubTaskCreationDTO dto)
      throws ValidationException, TaskNotFoundException {
    Set<UUID> idsBefore =
        manager.getAllTasksByClass(SubTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    manager.addTask(dto);
    return manager.getAllTasksByClass(SubTask.class).stream()
        .filter(t -> !idsBefore.contains(t.getId()))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Failed to retrieve added SubTask with title: " + dto.title()));
  }

  // --- Helper Methods for Assertions ---
  private void assertTaskState(
      Task task,
      String expectedTitle,
      String expectedDescription,
      TaskStatus expectedStatus,
      LocalDateTime expectedStartTime,
      Duration expectedDuration) {
    assertNotNull(task, "Task should not be null");
    assertEquals(expectedTitle, task.getTitle(), "Task title mismatch");
    assertEquals(expectedDescription, task.getDescription(), "Task description mismatch");
    assertEquals(expectedStatus, task.getStatus(), "Task status mismatch");
    if (expectedStartTime == null) {
      assertNull(task.getStartTime(), "Task start time should be null");
    } else {
      assertEquals(expectedStartTime, task.getStartTime(), "Task start time mismatch");
    }
    if (expectedDuration == null) {
      assertNull(task.getDuration(), "Task duration should be null");
    } else {
      assertEquals(expectedDuration, task.getDuration(), "Task duration mismatch");
    }
  }

  private void assertEpicState(
      EpicTask epic,
      String expectedTitle,
      String expectedDescription,
      TaskStatus expectedStatus,
      int expectedSubtaskCount) {
    assertTaskState(
        epic,
        expectedTitle,
        expectedDescription,
        expectedStatus,
        epic.getStartTime(),
        epic.getDuration()); // Epic start/duration can be derived
    assertEquals(expectedSubtaskCount, epic.getSubtaskIds().size(), "Epic subtask count mismatch");
  }

  // --- Constructor Tests ---
  @Test
  @DisplayName("Constructor should throw NullPointerException when TaskRepository is null")
  void testConstructor_NullRepository_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new TaskManagerImpl(null, historyManager));
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when HistoryManager is null")
  void testConstructor_NullHistoryManager_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new TaskManagerImpl(taskRepository, null));
  }

  // --- getAllTasks() Tests ---
  @Test
  @DisplayName("getAllTasks should return an empty collection when no tasks exist")
  void testGetAllTasks_NoTasks_ReturnsEmptyCollection() {
    assertTrue(manager.getAllTasks().isEmpty(), "Task collection should be empty");
  }

  @Test
  @DisplayName("getAllTasks should return all tasks of different types")
  void testGetAllTasks_WithMultipleTaskTypes_ReturnsAll()
      throws ValidationException, TaskNotFoundException {
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Reg1"));
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub1", epic1.getId()));

    assertEquals(3, manager.getAllTasks().size(), "Should return all 3 tasks");
  }

  // --- clearAllTasks() Tests ---
  @Test
  @DisplayName("clearAllTasks should do nothing if no tasks exist")
  void testClearAllTasks_NoTasks_RemainsEmpty() {
    manager.clearAllTasks();
    assertTrue(manager.getAllTasks().isEmpty(), "Task collection should remain empty");
    assertTrue(manager.getHistory().isEmpty(), "History should remain empty");
  }

  @Test
  @DisplayName("clearAllTasks should remove all tasks, clear history, and clear schedule")
  void testClearAllTasks_WithTasks_RemovesAllAndClearsHistoryAndSchedule()
      throws ValidationException, TaskNotFoundException {
    RegularTask reg1 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "Reg1", DEFAULT_START_TIME, DEFAULT_DURATION));
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTOWithTime(
            "Sub1", epic1.getId(), DEFAULT_START_TIME_2, DEFAULT_DURATION));

    manager.getTask(reg1.getId()); // Add to history
    assertFalse(manager.getHistory().isEmpty(), "History should not be empty before clear");
    assertFalse(
        manager.getPrioritizedTasks().isEmpty(),
        "Prioritized tasks should not be empty before clear");

    manager.clearAllTasks();

    assertTrue(manager.getAllTasks().isEmpty(), "All tasks should be cleared");
    assertTrue(manager.getHistory().isEmpty(), "History should be cleared");
    assertTrue(manager.getPrioritizedTasks().isEmpty(), "Prioritized tasks should be cleared");
  }

  // --- getPrioritizedTasks() Tests ---
  @Test
  @DisplayName("getPrioritizedTasks should return an empty list when no tasks exist")
  void testGetPrioritizedTasks_NoTasks_ReturnsEmptyList() {
    assertTrue(manager.getPrioritizedTasks().isEmpty());
  }

  @Test
  @DisplayName("getPrioritizedTasks should correctly order tasks by start time")
  void testGetPrioritizedTasks_TasksWithStartTime_ReturnsSortedByStartTime()
      throws ValidationException {
    RegularTask task1 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "Later", DEFAULT_START_TIME.plusHours(4), DEFAULT_DURATION));
    RegularTask task2 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "Earlier", DEFAULT_START_TIME, DEFAULT_DURATION));
    RegularTask task3 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "Middle", DEFAULT_START_TIME.plusHours(2), DEFAULT_DURATION));
    RegularTask taskNoTime = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("NoTime"));

    List<Task> prioritized = manager.getPrioritizedTasks();
    assertEquals(4, prioritized.size()); // Will include taskNoTime at the end due to nullsLast
    assertEquals(task2.getId(), prioritized.get(0).getId(), "Task2 (Earlier) should be first");
    assertEquals(task3.getId(), prioritized.get(1).getId(), "Task3 (Middle) should be second");
    assertEquals(task1.getId(), prioritized.get(2).getId(), "Task1 (Later) should be third");
    assertEquals(taskNoTime.getId(), prioritized.get(3).getId(), "TaskNoTime should be last");
  }

  @Test
  @DisplayName("getPrioritizedTasks: Tasks without start time should be at the end")
  void testGetPrioritizedTasks_TasksWithoutStartTime_AreLast() throws ValidationException {
    RegularTask taskWithTime =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "WithTime", DEFAULT_START_TIME, DEFAULT_DURATION));
    RegularTask taskWithoutTime1 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("NoTime1"));
    RegularTask taskWithoutTime2 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("NoTime2"));

    List<Task> prioritized = manager.getPrioritizedTasks();
    assertEquals(3, prioritized.size());
    assertEquals(taskWithTime.getId(), prioritized.get(0).getId());
    // Order of null start times is not strictly guaranteed beyond being last
    assertTrue(
        prioritized.subList(1, 3).stream()
            .map(Task::getId)
            .collect(Collectors.toSet())
            .containsAll(Set.of(taskWithoutTime1.getId(), taskWithoutTime2.getId())));
  }

  // --- removeTasksByType(Class<T> clazz) Tests ---
  @Test
  @DisplayName("removeTasksByType should throw NullPointerException if class type is null")
  void testRemoveTasksByType_NullClass_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.removeTasksByType(null));
  }

  @Test
  @DisplayName("removeTasksByType should remove only RegularTasks")
  void testRemoveTasksByType_RegularTask_RemovesOnlyRegularTasks()
      throws ValidationException, TaskNotFoundException {
    RegularTask reg1 = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Reg1"));
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub1", epic1.getId()));
    manager.getTask(reg1.getId()); // Add to history

    manager.removeTasksByType(RegularTask.class);

    assertTrue(manager.getAllTasksByClass(RegularTask.class).isEmpty());
    assertFalse(manager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertFalse(manager.getAllTasksByClass(SubTask.class).isEmpty());
    assertFalse(
        manager.getHistory().stream().anyMatch(t -> t.getId().equals(reg1.getId())),
        "Removed regular task should not be in history");
  }

  @Test
  @DisplayName("removeTasksByType should remove only SubTasks and update Epics to NEW status")
  void testRemoveTasksByType_SubTask_RemovesOnlySubTasksUpdatesEpics()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    SubTask sub1 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub1", epic1.getId()));
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Reg1"));
    manager.getTask(sub1.getId()); // Add to history

    manager.removeTasksByType(SubTask.class);

    assertTrue(manager.getAllTasksByClass(SubTask.class).isEmpty());
    assertFalse(manager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertFalse(manager.getAllTasksByClass(RegularTask.class).isEmpty());

    EpicTask updatedEpic1 = (EpicTask) manager.getTask(epic1.getId()).orElseThrow();
    assertTrue(
        updatedEpic1.getSubtaskIds().isEmpty(),
        "Epic should have no subtasks after SubTask removal by type");
    assertEquals(
        TaskStatus.NEW,
        updatedEpic1.getStatus(),
        "Epic status should be NEW after all its subtasks are removed");
    assertNull(updatedEpic1.getStartTime(), "Epic start time should be null");
    assertNull(updatedEpic1.getDuration(), "Epic duration should be null");
    assertFalse(
        manager.getHistory().stream().anyMatch(t -> t.getId().equals(sub1.getId())),
        "Removed subtask should not be in history");
  }

  @Test
  @DisplayName("removeTasksByType should remove EpicTasks and their SubTasks")
  void testRemoveTasksByType_EpicTask_RemovesEpicsAndTheirSubTasks()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    SubTask sub1 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub1", epic1.getId()));
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Reg1"));
    manager.getTask(epic1.getId()); // Add to history
    manager.getTask(sub1.getId()); // Add to history

    manager.removeTasksByType(EpicTask.class);

    assertTrue(manager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertTrue(
        manager.getAllTasksByClass(SubTask.class).isEmpty(),
        "Subtasks of removed epics should also be removed");
    assertFalse(manager.getAllTasksByClass(RegularTask.class).isEmpty());
    assertFalse(
        manager.getHistory().stream().anyMatch(t -> t.getId().equals(epic1.getId())),
        "Removed epic task should not be in history");
    assertFalse(
        manager.getHistory().stream().anyMatch(t -> t.getId().equals(sub1.getId())),
        "Removed subtask of epic should not be in history");
  }

  @Test
  @DisplayName("removeTasksByType should throw UnsupportedOperationException for base Task class")
  void testRemoveTasksByType_UnsupportedType_ThrowsUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> manager.removeTasksByType(Task.class));
  }

  @Test
  @DisplayName("removeTasksByType should do nothing if no tasks of the specified type exist")
  void testRemoveTasksByType_NoMatchingTasks_DoesNothing() throws ValidationException {
    addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    int initialTaskCount = manager.getAllTasks().size();

    manager.removeTasksByType(RegularTask.class); // No regular tasks exist

    assertEquals(initialTaskCount, manager.getAllTasks().size(), "Task count should not change");
  }

  // --- removeTaskById(UUID id) Tests ---
  @Test
  @DisplayName("removeTaskById should throw NullPointerException if ID is null")
  void testRemoveTaskById_NullId_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.removeTaskById(null));
  }

  @Test
  @DisplayName("removeTaskById should return empty Optional for a non-existent ID")
  void testRemoveTaskById_NonExistentId_ReturnsEmptyOptional()
      throws ValidationException, TaskNotFoundException {
    Optional<Task> removed = manager.removeTaskById(UUID.randomUUID());
    assertTrue(removed.isEmpty(), "Should return empty Optional for non-existent ID");
  }

  @Test
  @DisplayName("removeTaskById should remove a RegularTask and return it")
  void testRemoveTaskById_RegularTask_RemovesAndReturnsTask()
      throws ValidationException, TaskNotFoundException {
    RegularTask reg1 = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Reg1"));
    manager.getTask(reg1.getId()); // Add to history

    Optional<Task> removed = manager.removeTaskById(reg1.getId());

    assertTrue(removed.isPresent(), "Removed task should be returned");
    assertEquals(reg1.getId(), removed.get().getId(), "Returned task ID should match");
    assertFalse(manager.getTask(reg1.getId()).isPresent(), "Task should be removed from manager");
    assertFalse(
        manager.getHistory().stream().anyMatch(tv -> tv.getId().equals(reg1.getId())),
        "Task should be removed from history");
  }

  @Test
  @DisplayName("removeTaskById should remove a SubTask, update Epic, and return it")
  void testRemoveTaskById_SubTask_RemovesUpdatesEpicReturnsTask()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    SubTask sub1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTOWithTime(
                "Sub1", epic1.getId(), DEFAULT_START_TIME, DEFAULT_DURATION));
    SubTask sub2 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTOWithTime(
                "Sub2", epic1.getId(), DEFAULT_START_TIME_2, DEFAULT_DURATION));

    // Update sub1 to DONE to check epic status calculation after sub2 removal
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle(),
            sub1.getDescription(),
            TaskStatus.DONE,
            epic1.getId(),
            sub1.getStartTime(),
            sub1.getDuration()));
    manager.getTask(sub2.getId()); // Add sub2 to history

    Optional<Task> removed = manager.removeTaskById(sub2.getId());

    assertTrue(removed.isPresent());
    assertEquals(sub2.getId(), removed.get().getId());
    assertFalse(manager.getTask(sub2.getId()).isPresent());
    assertFalse(
        manager.getHistory().stream().anyMatch(tv -> tv.getId().equals(sub2.getId())),
        "Subtask should be removed from history");

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic1.getId()).orElseThrow();
    assertFalse(
        updatedEpic.getSubtaskIds().contains(sub2.getId()),
        "Removed subtask ID should not be in epic's subtask list");
    assertEquals(1, updatedEpic.getSubtaskIds().size(), "Epic should have one remaining subtask");
    assertEquals(
        TaskStatus.DONE,
        updatedEpic.getStatus(),
        "Epic status should be DONE as remaining subtask is DONE");
    assertEquals(
        sub1.getStartTime(),
        updatedEpic.getStartTime(),
        "Epic start time should be sub1's start time");
  }

  @Test
  @DisplayName("removeTaskById removing the last SubTask should set Epic to NEW")
  void testRemoveTaskById_LastSubTask_SetsEpicToNew()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic"));
    SubTask sub =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTOWithTime(
                "Sub", epic.getId(), DEFAULT_START_TIME, DEFAULT_DURATION));

    manager.removeTaskById(sub.getId());

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertTrue(updatedEpic.getSubtaskIds().isEmpty());
    assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
    assertNull(updatedEpic.getStartTime());
    assertNull(updatedEpic.getDuration());
  }

  @Test
  @DisplayName("removeTaskById should remove an EpicTask and its SubTasks, and return it")
  void testRemoveTaskById_EpicTask_RemovesEpicAndSubTasksReturnsTask()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("Epic1"));
    SubTask sub1 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub1", epic1.getId()));
    manager.getTask(epic1.getId()); // Add to history
    manager.getTask(sub1.getId()); // Add to history

    Optional<Task> removed = manager.removeTaskById(epic1.getId());

    assertTrue(removed.isPresent());
    assertEquals(epic1.getId(), removed.get().getId());
    assertFalse(manager.getTask(epic1.getId()).isPresent(), "Epic should be removed");
    assertFalse(
        manager.getTask(sub1.getId()).isPresent(),
        "Subtask of removed epic should also be removed");
    assertFalse(
        manager.getHistory().stream().anyMatch(tv -> tv.getId().equals(epic1.getId())),
        "Epic should be removed from history");
    assertFalse(
        manager.getHistory().stream().anyMatch(tv -> tv.getId().equals(sub1.getId())),
        "Subtask of Epic should be removed from history");
  }

  // --- getTask(UUID id) Tests ---
  @Test
  @DisplayName("getTask should throw NullPointerException if ID is null")
  void testGetTask_NullId_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.getTask(null));
  }

  @Test
  @DisplayName("getTask should return empty Optional for a non-existent ID")
  void testGetTask_NonExistentId_ReturnsEmptyOptional() {
    assertTrue(manager.getTask(UUID.randomUUID()).isEmpty());
  }

  @Test
  @DisplayName("getTask should return the task and add it to history")
  void testGetTask_ExistingTask_ReturnsTaskAndAddsToHistory() throws ValidationException {
    RegularTask reg1 = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Reg1"));
    Optional<Task> found = manager.getTask(reg1.getId());

    assertTrue(found.isPresent());
    assertEquals(reg1.getId(), found.get().getId());
    assertEquals(1, manager.getHistory().size());
    assertEquals(reg1.getId(), manager.getHistory().iterator().next().getId());
  }

  @Test
  @DisplayName("getTask: Accessing multiple tasks updates history correctly (LRU)")
  void testGetTask_AccessMultipleTimes_UpdatesHistoryCorrectly() throws ValidationException {
    RegularTask task1 = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Task1"));
    RegularTask task2 = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Task2"));

    manager.getTask(task1.getId()); // History: [task1]
    manager.getTask(task2.getId()); // History: [task1, task2]
    manager.getTask(task1.getId()); // History: [task2, task1] (task1 becomes most recent)

    List<UUID> historyIds = manager.getHistory().stream().map(Task::getId).toList();
    assertEquals(2, historyIds.size());
    assertEquals(task2.getId(), historyIds.get(0)); // task2 was accessed before task1's re-access
    assertEquals(task1.getId(), historyIds.get(1)); // task1 is now most recent
  }

  // --- addTask(RegularTaskCreationDTO dto) Tests ---
  @Test
  @DisplayName("addTask (Regular) should throw NullPointerException if DTO is null")
  void testAddRegularTask_NullDto_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.addTask((RegularTaskCreationDTO) null));
  }

  @Test
  @DisplayName("addTask (Regular) should add a valid RegularTask")
  void testAddRegularTask_ValidDto_AddsTask() throws ValidationException {
    RegularTaskCreationDTO dto =
        createValidRegularTaskCreationDTOWithTime("RegTime", DEFAULT_START_TIME, DEFAULT_DURATION);
    RegularTask created = addAndRetrieveRegularTask(dto);
    assertTaskState(
        created, dto.title(), dto.description(), TaskStatus.NEW, dto.startTime(), dto.duration());
    assertTrue(
        manager.getPrioritizedTasks().stream().anyMatch(t -> t.getId().equals(created.getId())),
        "Task should be in prioritized list");
  }

  @Test
  @DisplayName("addTask (Regular) should throw ValidationException for invalid title")
  void testAddRegularTask_InvalidTitle_ThrowsValidationException() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(INVALID_SHORT_TITLE, VALID_DESCRIPTION_PREFIX, null, null);
    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  @Test
  @DisplayName("addTask (Regular) should throw ValidationException for invalid description")
  void testAddRegularTask_InvalidDescription_ThrowsValidationException() {
    RegularTaskCreationDTO dto =
        new RegularTaskCreationDTO(VALID_TITLE_PREFIX, INVALID_SHORT_DESCRIPTION, null, null);
    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  @Test
  @DisplayName("addTask (Regular) should throw ValidationException on time overlap")
  void testAddRegularTask_TimeOverlap_ThrowsValidationException() throws ValidationException {
    addAndRetrieveRegularTask(
        createValidRegularTaskCreationDTOWithTime("Reg1", DEFAULT_START_TIME, DEFAULT_DURATION));
    RegularTaskCreationDTO overlappingDto =
        createValidRegularTaskCreationDTOWithTime(
            "Reg2Overlap", DEFAULT_START_TIME.plusHours(1), DEFAULT_DURATION); // Overlaps
    assertThrows(ValidationException.class, () -> manager.addTask(overlappingDto));
  }

  // --- addTask(EpicTaskCreationDTO dto) Tests ---
  @Test
  @DisplayName("addTask (Epic) should throw NullPointerException if DTO is null")
  void testAddEpicTask_NullDto_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.addTask((EpicTaskCreationDTO) null));
  }

  @Test
  @DisplayName("addTask (Epic) should add a valid EpicTask")
  void testAddEpicTask_ValidDto_AddsTask() throws ValidationException {
    EpicTaskCreationDTO dto = createValidEpicTaskCreationDTO("Epic1");
    EpicTask created = addAndRetrieveEpicTask(dto);
    assertEpicState(created, dto.title(), dto.description(), TaskStatus.NEW, 0);
    assertNull(
        created.getStartTime()); // Epic start time is null initially if not provided / no subtasks
  }

  @Test
  @DisplayName("addTask (Epic) with start time should add to schedule")
  void testAddEpicTask_WithStartTime_AddsToSchedule() throws ValidationException {
    EpicTaskCreationDTO dto =
        createValidEpicTaskCreationDTOWithTime("EpicTime", DEFAULT_START_TIME);
    EpicTask created = addAndRetrieveEpicTask(dto);
    assertEquals(DEFAULT_START_TIME, created.getStartTime());
    assertTrue(
        manager.getPrioritizedTasks().stream().anyMatch(t -> t.getId().equals(created.getId())));
  }

  @Test
  @DisplayName("addTask (Epic) should throw ValidationException for invalid title")
  void testAddEpicTask_InvalidTitle_ThrowsValidationException() {
    EpicTaskCreationDTO dto =
        new EpicTaskCreationDTO(INVALID_SHORT_TITLE, VALID_DESCRIPTION_PREFIX, null);
    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  // --- addTask(SubTaskCreationDTO dto) Tests ---
  @Test
  @DisplayName("addTask (SubTask) should throw NullPointerException if DTO is null")
  void testAddSubTask_NullDto_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.addTask((SubTaskCreationDTO) null));
  }

  @Test
  @DisplayName("addTask (SubTask) should add a valid SubTask and update Epic")
  void testAddSubTask_ValidDto_AddsTaskAndUpdateEpic()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("ParentEpic"));
    SubTaskCreationDTO subDto =
        createValidSubTaskCreationDTOWithTime(
            "Sub1", epic.getId(), DEFAULT_START_TIME, DEFAULT_DURATION);

    SubTask createdSub = addAndRetrieveSubTask(subDto);
    assertTaskState(
        createdSub,
        subDto.title(),
        subDto.description(),
        TaskStatus.NEW,
        subDto.startTime(),
        subDto.duration());
    assertEquals(epic.getId(), createdSub.getEpicTaskId());

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertTrue(updatedEpic.getSubtaskIds().contains(createdSub.getId()));
    assertEquals(
        TaskStatus.NEW,
        updatedEpic.getStatus(),
        "Epic status should be NEW with one NEW subtask"); // Or IN_PROGRESS depending on logic for
    // single subtask
    assertEquals(DEFAULT_START_TIME, updatedEpic.getStartTime(), "Epic start time should update");
    assertEquals(DEFAULT_DURATION, updatedEpic.getDuration(), "Epic duration should update");
  }

  @Test
  @DisplayName("addTask (SubTask) should throw ValidationException for non-existent Epic ID")
  void testAddSubTask_NonExistentEpicId_ThrowsValidationException() {
    SubTaskCreationDTO dto = createValidSubTaskCreationDTO("SubNoEpic", UUID.randomUUID());
    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  @Test
  @DisplayName(
      "addTask (SubTask) should throw ValidationException if target Epic ID is not an Epic")
  void testAddSubTask_TargetEpicIdIsNotEpic_ThrowsValidationException() throws ValidationException {
    RegularTask notAnEpic =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("NotAnEpic"));
    SubTaskCreationDTO subDto = createValidSubTaskCreationDTO("SubToRegular", notAnEpic.getId());
    assertThrows(ValidationException.class, () -> manager.addTask(subDto));
  }

  @Test
  @DisplayName("addTask (SubTask) should throw ValidationException on time overlap")
  void testAddSubTask_TimeOverlap_ThrowsValidationException()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicForOverlap"));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTOWithTime(
            "Sub1", epic.getId(), DEFAULT_START_TIME, DEFAULT_DURATION));

    SubTaskCreationDTO overlappingDto =
        createValidSubTaskCreationDTOWithTime(
            "Sub2Overlap", epic.getId(), DEFAULT_START_TIME.plusHours(1), DEFAULT_DURATION);
    assertThrows(ValidationException.class, () -> manager.addTask(overlappingDto));
  }

  // --- updateTask(RegularTaskUpdateDTO dto) Tests ---
  @Test
  @DisplayName("updateTask (Regular) should throw NullPointerException if DTO is null")
  void testUpdateRegularTask_NullDto_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.updateTask((RegularTaskUpdateDTO) null));
  }

  @Test
  @DisplayName("updateTask (Regular) should update an existing RegularTask")
  void testUpdateRegularTask_ValidDto_UpdatesTask()
      throws ValidationException, TaskNotFoundException {
    RegularTask original =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "OriginalReg", DEFAULT_START_TIME, DEFAULT_DURATION));
    RegularTaskUpdateDTO updateDto =
        new RegularTaskUpdateDTO(
            original.getId(),
            "Updated Title",
            "Updated Description",
            TaskStatus.IN_PROGRESS,
            DEFAULT_START_TIME_2,
            DEFAULT_DURATION.plusHours(1));

    manager.updateTask(updateDto);
    RegularTask updated = (RegularTask) manager.getTask(updateDto.id()).orElse(null);
    assertTaskState(
        updated,
        updateDto.title(),
        updateDto.description(),
        updateDto.status(),
        updateDto.startTime(),
        updateDto.duration());
    assertNotEquals(original.getUpdateDate(), updated.getUpdateDate());
  }

  @Test
  @DisplayName("updateTask (Regular) should throw ValidationException for non-existent ID")
  void testUpdateRegularTask_NonExistentId_ThrowsValidationException() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(UUID.randomUUID(), "T", "D", TaskStatus.NEW, null, null);
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName(
      "updateTask (Regular) should throw ValidationException if ID is for a different task type")
  void testUpdateRegularTask_UpdatingWrongType_ThrowsValidationException()
      throws ValidationException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicToUpdateAsRegular"));
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            epic.getId(), "Updated Title", "Updated Desc", TaskStatus.DONE, null, null);
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName("updateTask (Regular) should throw ValidationException on time overlap")
  void testUpdateRegularTask_TimeOverlap_ThrowsValidationException() throws ValidationException {
    RegularTask task1 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "Reg1Time", DEFAULT_START_TIME, DEFAULT_DURATION));
    RegularTask task2 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTOWithTime(
                "Reg2Time", DEFAULT_START_TIME_2, DEFAULT_DURATION));

    RegularTaskUpdateDTO updateDtoOverlapping =
        new RegularTaskUpdateDTO(
            task2.getId(),
            task2.getTitle(),
            task2.getDescription(),
            task2.getStatus(),
            DEFAULT_START_TIME.plusHours(1),
            DEFAULT_DURATION); // Overlaps task1
    assertThrows(ValidationException.class, () -> manager.updateTask(updateDtoOverlapping));
  }

  // --- updateTask(SubTaskUpdateDTO dto) Tests ---
  @Test
  @DisplayName("updateTask (SubTask) should update an existing SubTask and its Epic")
  void testUpdateSubTask_ValidDto_UpdatesTaskAndEpic()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicForSubUpdate"));
    SubTask sub1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTOWithTime(
                "SubOriginal", epic.getId(), DEFAULT_START_TIME, DEFAULT_DURATION));

    SubTaskUpdateDTO updateDto =
        new SubTaskUpdateDTO(
            sub1.getId(),
            "Sub Updated",
            "Sub Desc Updated",
            TaskStatus.DONE,
            epic.getId(),
            DEFAULT_START_TIME_2,
            DEFAULT_DURATION.plusHours(1));
    manager.updateTask(updateDto);
    SubTask updatedSub = (SubTask) manager.getTask(updateDto.id()).orElse(null);

    assertTaskState(
        updatedSub,
        updateDto.title(),
        updateDto.description(),
        updateDto.status(),
        updateDto.startTime(),
        updateDto.duration());

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.DONE, updatedEpic.getStatus(), "Epic status should be DONE");
    assertEquals(DEFAULT_START_TIME_2, updatedEpic.getStartTime());
  }

  @Test
  @DisplayName("updateTask (SubTask) changing status updates Epic status correctly")
  void testUpdateSubTask_StatusChange_UpdatesEpicStatusCorrectly()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicStatusTest"));
    SubTask sub1 =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub1Status", epic.getId())); // NEW
    SubTask sub2 =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("Sub2Status", epic.getId())); // NEW

    EpicTask currentEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.NEW, currentEpic.getStatus(), "Initial Epic status should be NEW");

    // sub1 -> IN_PROGRESS, sub2 -> NEW  => Epic IN_PROGRESS
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle(),
            sub1.getDescription(),
            TaskStatus.IN_PROGRESS,
            epic.getId(),
            null,
            null));
    currentEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(
        TaskStatus.IN_PROGRESS,
        currentEpic.getStatus(),
        "Epic status should be IN_PROGRESS (one IN_PROGRESS, one NEW)");

    // sub1 -> DONE, sub2 -> NEW => Epic IN_PROGRESS
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle(),
            sub1.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));
    currentEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(
        TaskStatus.IN_PROGRESS,
        currentEpic.getStatus(),
        "Epic status should be IN_PROGRESS (one DONE, one NEW)");

    // sub1 -> DONE, sub2 -> DONE => Epic DONE
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub2.getId(),
            sub2.getTitle(),
            sub2.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));
    currentEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(
        TaskStatus.DONE, currentEpic.getStatus(), "Epic status should be DONE (both DONE)");
  }

  @Test
  @DisplayName("updateTask (SubTask) should throw ValidationException for non-existent SubTask ID")
  void testUpdateSubTask_NonExistentId_ThrowsValidationException() {
    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            UUID.randomUUID(), "T", "D", TaskStatus.NEW, UUID.randomUUID(), null, null);
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName(
      "updateTask (SubTask) should throw ValidationException for non-existent Epic ID in DTO")
  void testUpdateSubTask_NonExistentEpicIdInDto_ThrowsValidationException()
      throws ValidationException, TaskNotFoundException {
    EpicTask realEpic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("RealEpic"));
    SubTask sub =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubForEpicUpdate", realEpic.getId()));

    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            sub.getId(),
            "T",
            "D",
            TaskStatus.NEW,
            UUID.randomUUID(),
            null,
            null); // Non-existent epicId
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  // --- updateTask(EpicTaskUpdateDTO dto) Tests ---
  @Test
  @DisplayName("updateTask (Epic) should update an existing EpicTask (title, description)")
  void testUpdateEpicTask_ValidDto_UpdatesTask() throws ValidationException, TaskNotFoundException {
    EpicTask originalEpic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("OriginalEpic"));
    SubTask sub =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO(
                "SubForEpic", originalEpic.getId())); // To check subtasks and status remain
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub.getId(),
            sub.getTitle(),
            sub.getDescription(),
            TaskStatus.IN_PROGRESS,
            originalEpic.getId(),
            null,
            null));

    EpicTaskUpdateDTO updateDto =
        new EpicTaskUpdateDTO(
            originalEpic.getId(), "Epic Updated Title", "Epic Updated Description");
    manager.updateTask(updateDto);
    EpicTask updatedEpic = (EpicTask) manager.getTask(updateDto.id()).orElseThrow();

    assertEquals(updateDto.title(), updatedEpic.getTitle());
    assertEquals(updateDto.description(), updatedEpic.getDescription());
    assertEquals(
        TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Epic status should not change from DTO");
    assertEquals(
        1, updatedEpic.getSubtaskIds().size(), "Epic subtask count should not change from DTO");
    assertTrue(updatedEpic.getSubtaskIds().contains(sub.getId()));
  }

  @Test
  @DisplayName("updateTask (Epic) should throw ValidationException for non-existent ID")
  void testUpdateEpicTask_NonExistentId_ThrowsValidationException() {
    EpicTaskUpdateDTO dto = new EpicTaskUpdateDTO(UUID.randomUUID(), "T", "D");
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  // --- getEpicSubtasks(UUID epicId) Tests ---
  @Test
  @DisplayName("getEpicSubtasks should throw NullPointerException for null Epic ID")
  void testGetEpicSubtasks_NullEpicId_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.getEpicSubtasks(null));
  }

  @Test
  @DisplayName("getEpicSubtasks should throw ValidationException for non-existent Epic ID")
  void testGetEpicSubtasks_NonExistentEpicId_ThrowsValidationException() {
    assertThrows(ValidationException.class, () -> manager.getEpicSubtasks(UUID.randomUUID()));
  }

  @Test
  @DisplayName("getEpicSubtasks should throw ValidationException if ID is not for an Epic")
  void testGetEpicSubtasks_IdIsNotForEpic_ThrowsValidationException() throws ValidationException {
    RegularTask notAnEpic =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("NotAnEpicForSubtasks"));
    assertThrows(ValidationException.class, () -> manager.getEpicSubtasks(notAnEpic.getId()));
  }

  @Test
  @DisplayName("getEpicSubtasks should return empty collection for Epic with no SubTasks")
  void testGetEpicSubtasks_EpicHasNoSubtasks_ReturnsEmptyCollection() throws ValidationException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicNoSubs"));
    assertTrue(manager.getEpicSubtasks(epic.getId()).isEmpty());
  }

  @Test
  @DisplayName("getEpicSubtasks should return all SubTasks for a given Epic")
  void testGetEpicSubtasks_EpicHasSubtasks_ReturnsSubtasks()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicWithSubs"));
    SubTask sub1 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubA", epic.getId()));
    SubTask sub2 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubB", epic.getId()));

    Collection<SubTask> subtasks = manager.getEpicSubtasks(epic.getId());
    assertEquals(2, subtasks.size());
    Set<UUID> subtaskIds = subtasks.stream().map(SubTask::getId).collect(Collectors.toSet());
    assertTrue(subtaskIds.contains(sub1.getId()));
    assertTrue(subtaskIds.contains(sub2.getId()));
  }

  // --- getAllTasksByClass(Class<T> targetClass) Tests ---
  @Test
  @DisplayName("getAllTasksByClass should throw NullPointerException for null class type")
  void testGetAllTasksByClass_NullClass_ThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> manager.getAllTasksByClass(null));
  }

  @Test
  @DisplayName("getAllTasksByClass should return only RegularTasks")
  void testGetAllTasksByClass_RegularTask_ReturnsOnlyRegularTasks()
      throws ValidationException, TaskNotFoundException {
    RegularTask reg1 = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("R1"));
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("E1"));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("S1", epic1.getId()));

    Collection<RegularTask> regularTasks = manager.getAllTasksByClass(RegularTask.class);
    assertEquals(1, regularTasks.size());
    assertEquals(reg1.getId(), regularTasks.iterator().next().getId());
  }

  @Test
  @DisplayName("getAllTasksByClass for base Task class should return all tasks")
  void testGetAllTasksByClass_BaseTaskClass_ReturnsAllTasks()
      throws ValidationException, TaskNotFoundException {
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("R1"));
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("E1"));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("S1", epic1.getId()));

    Collection<Task> allTasks = manager.getAllTasksByClass(Task.class);
    assertEquals(3, allTasks.size());
  }

  @Test
  @DisplayName("getAllTasksByClass should return empty collection if no tasks of type exist")
  void testGetAllTasksByClass_NoMatchingTasks_ReturnsEmptyCollection() throws ValidationException {
    addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("E1"));
    assertTrue(manager.getAllTasksByClass(RegularTask.class).isEmpty());
  }

  // --- HistoryManager Interaction Tests ---
  @Test
  @DisplayName("getHistory should initially be empty")
  void testGetHistory_Initial_IsEmpty() {
    assertTrue(manager.getHistory().isEmpty());
  }

  @Test
  @DisplayName("getHistory should not contain deleted tasks")
  void testGetHistory_AfterRemoveTask_AccessedTaskIsRemovedFromHistory()
      throws ValidationException, TaskNotFoundException {
    RegularTask task = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("HistoryTask"));
    manager.getTask(task.getId()); // Add to history
    assertTrue(manager.getHistory().stream().anyMatch(tv -> tv.getId().equals(task.getId())));

    manager.removeTaskById(task.getId());
    assertFalse(manager.getHistory().stream().anyMatch(tv -> tv.getId().equals(task.getId())));
  }

  @Test
  @DisplayName("getHistory: Accessing a non-existent task should not change history")
  void testGetHistory_AccessNonExistentTask_HistoryUnchanged() throws ValidationException {
    RegularTask task = addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("Existing"));
    manager.getTask(task.getId());
    int historySizeBefore = manager.getHistory().size();

    manager.getTask(UUID.randomUUID()); // Access non-existent

    assertEquals(historySizeBefore, manager.getHistory().size());
  }

  // --- Complex Scenarios ---
  @Test
  @DisplayName("Epic status should be NEW if all SubTasks are NEW")
  void testEpicStatus_AllSubTasksNew_EpicIsNew() throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicAllNew"));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubNew1", epic.getId()));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubNew2", epic.getId()));

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
  }

  @Test
  @DisplayName("Epic status should be DONE if all SubTasks are DONE")
  void testEpicStatus_AllSubTasksDone_EpicIsDone()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicAllDone"));
    SubTask sub1 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubDone1", epic.getId()));
    SubTask sub2 = addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubDone2", epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle(),
            sub1.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub2.getId(),
            sub2.getTitle(),
            sub2.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
  }

  @Test
  @DisplayName("Epic status should be IN_PROGRESS if SubTasks have mixed statuses (NEW and DONE)")
  void testEpicStatus_MixedNewAndDoneSubTasks_EpicIsInProgress()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicMixedNewDone"));
    SubTask subNew =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubNewMix", epic.getId()));
    SubTask subDone =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubDoneMix", epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            subDone.getId(),
            subDone.getTitle(),
            subDone.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));
    // subNew remains NEW

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
  }

  @Test
  @DisplayName(
      "Epic status should be IN_PROGRESS if SubTasks have mixed statuses (NEW and IN_PROGRESS)")
  void testEpicStatus_MixedNewAndInProgressSubTasks_EpicIsInProgress()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicMixedNewProgress"));
    SubTask subNew =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubNewMixP", epic.getId()));
    SubTask subInProgress =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubProgressMixP", epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            subInProgress.getId(),
            subInProgress.getTitle(),
            subInProgress.getDescription(),
            TaskStatus.IN_PROGRESS,
            epic.getId(),
            null,
            null));

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
  }

  @Test
  @DisplayName(
      "Epic status should be IN_PROGRESS if SubTasks have mixed statuses (IN_PROGRESS and DONE)")
  void testEpicStatus_MixedInProgressAndDoneSubTasks_EpicIsInProgress()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicMixedProgressDone"));
    SubTask subInProgress =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubProgressMixD", epic.getId()));
    SubTask subDone =
        addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubDoneMixD", epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            subInProgress.getId(),
            subInProgress.getTitle(),
            subInProgress.getDescription(),
            TaskStatus.IN_PROGRESS,
            epic.getId(),
            null,
            null));
    manager.updateTask(
        new SubTaskUpdateDTO(
            subDone.getId(),
            subDone.getTitle(),
            subDone.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
  }

  @Test
  @DisplayName("Epic time calculation: should be earliest start and latest end of subtasks")
  void testEpicTimeCalculation_AggregatesSubTaskTimes()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicTimeAgg"));
    LocalDateTime start1 = DEFAULT_START_TIME;
    Duration dur1 = Duration.ofHours(2); // ends at start1 + 2h
    LocalDateTime start2 = DEFAULT_START_TIME.plusHours(2);
    Duration dur2 = Duration.ofHours(3); // ends at start2 + 3h = start1 + 4h
    LocalDateTime start3 = DEFAULT_START_TIME.minusHours(1);
    Duration dur3 = Duration.ofHours(1); // ends at start3 + 1h = start1

    addAndRetrieveSubTask(
        createValidSubTaskCreationDTOWithTime("SubT1", epic.getId(), start1, dur1));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTOWithTime("SubT2", epic.getId(), start2, dur2));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTOWithTime("SubT3", epic.getId(), start3, dur3));

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();

    assertEquals(
        start3,
        updatedEpic.getStartTime(),
        "Epic start time should be the earliest subtask start time");
    // Expected end time is start2 + dur2 = DEFAULT_START_TIME + 1h + 3h = DEFAULT_START_TIME + 4h
    LocalDateTime expectedEndTime = start2.plus(dur2);
    Duration expectedEpicDuration = Duration.between(start3, expectedEndTime);
    assertEquals(
        expectedEpicDuration,
        updatedEpic.getDuration(),
        "Epic duration should span from earliest start to latest end");
  }

  @Test
  @DisplayName("Epic time calculation: if one subtask has no time, it's ignored for epic time")
  void testEpicTimeCalculation_IgnoreSubTaskWithNoTime()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicTimeIgnore"));
    LocalDateTime subTimeStart = DEFAULT_START_TIME;
    Duration subTimeDuration = DEFAULT_DURATION;

    addAndRetrieveSubTask(
        createValidSubTaskCreationDTOWithTime(
            "SubWithTime", epic.getId(), subTimeStart, subTimeDuration));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubWithoutTime", epic.getId())); // No time

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertEquals(subTimeStart, updatedEpic.getStartTime());
    assertEquals(subTimeDuration, updatedEpic.getDuration());
  }

  @Test
  @DisplayName("Epic time calculation: if all subtasks have no time, epic has no time")
  void testEpicTimeCalculation_AllSubTasksNoTime_EpicNoTime()
      throws ValidationException, TaskNotFoundException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicAllNoTime"));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubNoTime1", epic.getId()));
    addAndRetrieveSubTask(createValidSubTaskCreationDTO("SubNoTime2", epic.getId()));

    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertNull(updatedEpic.getStartTime());
    assertNull(updatedEpic.getDuration());
  }
}
