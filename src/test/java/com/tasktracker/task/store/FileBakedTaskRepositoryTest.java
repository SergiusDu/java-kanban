package com.tasktracker.task.store;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.cvs.TaskCsvMapper; // Импорт для доступа к CSV_HEADER
import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.InMemoryHistoryManager;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.manager.TaskManagerImpl;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.*;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

class FileBakedTaskRepositoryTest {

  private static final String VALID_TITLE_PREFIX = "Valid Task Title ";
  private static final String VALID_DESCRIPTION_PREFIX =
      "Valid Task Description with enough characters ";
  private static final String INVALID_SHORT_TITLE = "Short";
  private static final String INVALID_SHORT_DESCRIPTION = "ShortDesc";
  private static final LocalDateTime DEFAULT_START_TIME =
      LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
  private static final Duration DEFAULT_DURATION = Duration.ofHours(2);
  private static final LocalDateTime DEFAULT_START_TIME_2 = DEFAULT_START_TIME.plusHours(3);

  private TaskManager manager;
  @TempDir static Path tempDir;
  private static Path testDataFile;

  @BeforeAll
  static void beforeAll() {
    testDataFile = tempDir.resolve("task_data_test.csv");
  }

  @BeforeEach
  void setUp() throws IOException {
    Files.deleteIfExists(testDataFile);
    manager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
  }

  @AfterEach
  void cleanUp() throws IOException {
    if (manager != null) {
      manager.clearAllTasks();
    }
    Files.deleteIfExists(testDataFile);
  }

  private RegularTaskCreationDTO createValidRegularTaskCreationDTO(
      String titleSuffix, LocalDateTime startTime, Duration duration) {
    return new RegularTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix,
        VALID_DESCRIPTION_PREFIX + titleSuffix,
        startTime,
        duration);
  }

  private EpicTaskCreationDTO createValidEpicTaskCreationDTO(
      String titleSuffix, LocalDateTime startTime) {
    return new EpicTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix, VALID_DESCRIPTION_PREFIX + titleSuffix, startTime);
  }

  private SubTaskCreationDTO createValidSubTaskCreationDTO(
      String titleSuffix, UUID epicId, LocalDateTime startTime, Duration duration) {
    return new SubTaskCreationDTO(
        VALID_TITLE_PREFIX + titleSuffix,
        VALID_DESCRIPTION_PREFIX + titleSuffix,
        epicId,
        startTime,
        duration);
  }

  private <T extends Task> T findAddedTask(
      TaskManager currentManager, Class<T> taskClass, Set<UUID> idsBefore) {
    // Добавим небольшую задержку и повторные попытки, если это FileBakedTaskRepository,
    // чтобы дать файловой системе время на синхронизацию, хотя это больше костыль.
    // Основная проблема должна решаться корректной логикой load/save.
    for (int i = 0; i < 3; i++) { // Попробовать несколько раз
      Optional<T> foundTask =
          currentManager.getAllTasksByClass(taskClass).stream()
              .filter(t -> !idsBefore.contains(t.getId()))
              .findFirst();
      if (foundTask.isPresent()) {
        return foundTask.get();
      }
      try {
        Thread.sleep(50); // Короткая пауза
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    throw new NoSuchElementException(
        "Could not retrieve added "
            + taskClass.getSimpleName()
            + ". IDs before: "
            + idsBefore
            + ", IDs after: "
            + currentManager.getAllTasksByClass(taskClass).stream()
                .map(Task::getId)
                .collect(Collectors.toSet()));
  }

  private RegularTask addAndRetrieveRegularTask(RegularTaskCreationDTO dto)
      throws ValidationException, OverlapException {
    Set<UUID> idsBefore =
        manager.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    manager.addTask(dto);
    return findAddedTask(manager, RegularTask.class, idsBefore);
  }

  private EpicTask addAndRetrieveEpicTask(EpicTaskCreationDTO dto) throws ValidationException {
    Set<UUID> idsBefore =
        manager.getAllTasksByClass(EpicTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    manager.addTask(dto);
    return findAddedTask(manager, EpicTask.class, idsBefore);
  }

  private SubTask addAndRetrieveSubTask(SubTaskCreationDTO dto)
      throws ValidationException, TaskNotFoundException, OverlapException {
    Set<UUID> idsBefore =
        manager.getAllTasksByClass(SubTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    manager.addTask(dto);
    return findAddedTask(manager, SubTask.class, idsBefore);
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when TaskRepository is null")
  void testConstructorThrowsOnNullRepository() {
    assertThrows(
        NullPointerException.class,
        () -> new TaskManagerImpl(null, new InMemoryHistoryManager(new InMemoryHistoryStore())));
  }

  @Test
  @DisplayName("Should retrieve all tasks, also checking persistence")
  void testGetAllTasks() throws ValidationException, OverlapException {
    addAndRetrieveRegularTask(
        createValidRegularTaskCreationDTO("Task1", DEFAULT_START_TIME, DEFAULT_DURATION));
    addAndRetrieveRegularTask(
        createValidRegularTaskCreationDTO("Task2", DEFAULT_START_TIME_2, DEFAULT_DURATION));

    Collection<Task> tasks = manager.getAllTasks();
    assertEquals(2, tasks.size());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Collection<Task> loadedTasks = newManager.getAllTasks();
    assertEquals(2, loadedTasks.size());
  }

  @Test
  @DisplayName("Should clear all tasks, also checking file persistence")
  void testClearAllTasks() throws ValidationException, IOException, OverlapException {
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("ToClear", null, null));
    manager.clearAllTasks();
    assertEquals(0, manager.getAllTasks().size());

    List<String> lines = Files.readAllLines(testDataFile);
    assertEquals(
        1,
        lines.size(),
        "File should only contain the header after clearing tasks. Content:\n"
            + String.join("\n", lines));
    // Сравниваем первую часть заголовка для большей надежности
    assertTrue(
        lines.get(0).trim().startsWith(TaskCsvMapper.CSV_HEADER.split(",")[0].trim()),
        "File header is missing or incorrect. Expected to start with: "
            + TaskCsvMapper.CSV_HEADER.split(",")[0]);

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertEquals(0, newManager.getAllTasks().size(), "No tasks should be loaded from cleared file");
  }

  @Test
  @DisplayName("Should throw ValidationException for invalid RegularTask title or description")
  void testAddRegularTaskWithInvalidData() {
    RegularTaskCreationDTO invalidDtoShortTitle =
        new RegularTaskCreationDTO(INVALID_SHORT_TITLE, VALID_DESCRIPTION_PREFIX + "1", null, null);
    assertThrows(ValidationException.class, () -> manager.addTask(invalidDtoShortTitle));

    RegularTaskCreationDTO invalidDtoShortDesc =
        new RegularTaskCreationDTO(VALID_TITLE_PREFIX + "1", INVALID_SHORT_DESCRIPTION, null, null);
    assertThrows(ValidationException.class, () -> manager.addTask(invalidDtoShortDesc));
  }

  @Test
  @DisplayName("Should add a new RegularTask and persist it")
  void testAddRegularTask() throws ValidationException, OverlapException {
    RegularTaskCreationDTO dto =
        createValidRegularTaskCreationDTO("AddReg", DEFAULT_START_TIME, DEFAULT_DURATION);
    RegularTask created = addAndRetrieveRegularTask(dto);

    assertNotNull(created);
    assertEquals(dto.title(), created.getTitle());
    assertEquals(dto.description(), created.getDescription());
    assertEquals(TaskStatus.NEW, created.getStatus());
    assertEquals(dto.startTime(), created.getStartTime());
    assertEquals(dto.duration(), created.getDuration());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Optional<Task> loadedTask = newManager.getTask(created.getId());
    assertTrue(loadedTask.isPresent(), "Loaded task should be present");
    assertEquals(created.getTitle(), loadedTask.get().getTitle());
  }

  @Test
  @DisplayName("Should add a new EpicTask and persist it")
  void testAddEpicTask() throws ValidationException {
    EpicTaskCreationDTO dto = createValidEpicTaskCreationDTO("AddEpic", DEFAULT_START_TIME);
    EpicTask created = addAndRetrieveEpicTask(dto);

    assertNotNull(created);
    assertEquals(dto.title(), created.getTitle());
    assertEquals(dto.description(), created.getDescription());
    assertEquals(TaskStatus.NEW, created.getStatus());
    assertTrue(created.getSubtaskIds().isEmpty(), "Newly created epic should have no subtasks");
    assertEquals(dto.startTime(), created.getStartTime());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Optional<Task> loadedTask = newManager.getTask(created.getId());
    assertTrue(loadedTask.isPresent(), "Loaded epic should be present");
    assertEquals(created.getTitle(), loadedTask.get().getTitle());
  }

  @Test
  @DisplayName("Should add a SubTask to an existing EpicTask and persist changes")
  void testAddSubTask() throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask initialEpic =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("ParentEpicForSub", null));
    SubTaskCreationDTO subDto =
        createValidSubTaskCreationDTO(
            "AddSub", initialEpic.getId(), DEFAULT_START_TIME, DEFAULT_DURATION);
    SubTask createdSub = addAndRetrieveSubTask(subDto);

    assertNotNull(createdSub);
    assertEquals(subDto.title(), createdSub.getTitle());
    assertEquals(initialEpic.getId(), createdSub.getEpicTaskId());

    EpicTask updatedEpic =
        (EpicTask)
            manager
                .getTask(initialEpic.getId())
                .orElseThrow(() -> new AssertionError("Epic task not found after adding subtask"));
    assertTrue(
        updatedEpic.getSubtaskIds().contains(createdSub.getId()),
        "Updated epic should contain the new subtask ID");
    assertEquals(
        DEFAULT_START_TIME,
        updatedEpic.getStartTime(),
        "Epic start time should update based on subtask");

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Optional<Task> loadedSub = newManager.getTask(createdSub.getId());
    assertTrue(loadedSub.isPresent(), "Loaded subtask should be present");
    assertEquals(createdSub.getTitle(), loadedSub.get().getTitle());

    Optional<Task> loadedEpicOptional = newManager.getTask(initialEpic.getId());
    assertTrue(loadedEpicOptional.isPresent(), "Loaded epic should be present");
    EpicTask loadedEpic = (EpicTask) loadedEpicOptional.get();
    assertTrue(
        loadedEpic.getSubtaskIds().contains(createdSub.getId()),
        "Loaded epic should contain the subtask ID");
    assertEquals(
        DEFAULT_START_TIME, loadedEpic.getStartTime(), "Loaded epic start time should be correct");
  }

  @Test
  @DisplayName("Should throw ValidationException when adding SubTask to nonexistent EpicTask")
  void testAddSubTaskToNonexistentEpic() {
    SubTaskCreationDTO dto =
        createValidSubTaskCreationDTO(
            "SubToNonExist", UUID.randomUUID(), DEFAULT_START_TIME, DEFAULT_DURATION);
    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  @Test
  @DisplayName("Should update an existing RegularTask and persist it")
  void testUpdateRegularTask() throws ValidationException, TaskNotFoundException, OverlapException {
    RegularTask task =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("RegUpdateOrig", null, null));
    RegularTaskUpdateDTO updateDto =
        new RegularTaskUpdateDTO(
            task.getId(),
            VALID_TITLE_PREFIX + "RegUpdateMod",
            VALID_DESCRIPTION_PREFIX + "RegUpdateMod",
            TaskStatus.IN_PROGRESS,
            DEFAULT_START_TIME,
            DEFAULT_DURATION);

    manager.updateTask(updateDto);
    RegularTask updated = (RegularTask) manager.getTask(updateDto.id()).orElseThrow();
    assertEquals(updateDto.title(), updated.getTitle());
    assertEquals(updateDto.description(), updated.getDescription());
    assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Optional<Task> loadedTaskOpt = newManager.getTask(task.getId());
    assertTrue(loadedTaskOpt.isPresent());
    Task loadedTask = loadedTaskOpt.get();
    assertEquals(updateDto.title(), loadedTask.getTitle());
    assertEquals(TaskStatus.IN_PROGRESS, loadedTask.getStatus());
  }

  @Test
  @DisplayName("Should throw ValidationException when updating RegularTask with invalid data")
  void testUpdateRegularTaskWithInvalidData() throws ValidationException, OverlapException {
    RegularTask task =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTO("RegUpdateInvalid", null, null));
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            task.getId(),
            INVALID_SHORT_TITLE,
            VALID_DESCRIPTION_PREFIX + "ValidDesc",
            TaskStatus.IN_PROGRESS,
            null,
            null);
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName("Should calculate EpicTask status correctly and persist changes")
  void testCalculateEpicTaskStatus()
      throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask epicTask =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicStatusCalc", null));
    SubTask subTask1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubStat1", epicTask.getId(), null, null));
    SubTask subTask2 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubStat2", epicTask.getId(), null, null));

    manager.updateTask(
        new SubTaskUpdateDTO(
            subTask1.getId(),
            subTask1.getTitle(),
            subTask1.getDescription(),
            TaskStatus.DONE,
            epicTask.getId(),
            null,
            null));
    EpicTask updatedEpicAfterSub1Done = (EpicTask) manager.getTask(epicTask.getId()).orElseThrow();
    assertEquals(TaskStatus.IN_PROGRESS, updatedEpicAfterSub1Done.getStatus());

    TaskManager midManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    EpicTask midLoadedEpic = (EpicTask) midManager.getTask(epicTask.getId()).orElseThrow();
    assertEquals(TaskStatus.IN_PROGRESS, midLoadedEpic.getStatus());

    manager.updateTask(
        new SubTaskUpdateDTO(
            subTask2.getId(),
            subTask2.getTitle(),
            subTask2.getDescription(),
            TaskStatus.DONE,
            epicTask.getId(),
            null,
            null));
    EpicTask updatedEpicAfterSub2Done = (EpicTask) manager.getTask(epicTask.getId()).orElseThrow();
    assertEquals(TaskStatus.DONE, updatedEpicAfterSub2Done.getStatus());

    TaskManager finalManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    EpicTask finalLoadedEpic = (EpicTask) finalManager.getTask(epicTask.getId()).orElseThrow();
    assertEquals(TaskStatus.DONE, finalLoadedEpic.getStatus());
  }

  @Test
  @DisplayName("Should remove a RegularTask by ID, return it, and persist removal")
  void testRemoveRegularTaskById()
      throws ValidationException, TaskNotFoundException, OverlapException {
    RegularTask created =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("RegRemove", null, null));
    Optional<Task> removed = manager.removeTaskById(created.getId());

    assertTrue(removed.isPresent());
    assertEquals(created.getId(), removed.get().getId());
    assertFalse(manager.getTask(created.getId()).isPresent());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertFalse(
        newManager.getTask(created.getId()).isPresent(),
        "Removed task should not be in new manager");
  }

  @Test
  @DisplayName("Should remove a SubTask by ID, verify EpicTask status updates, and persist")
  void testRemoveSubTaskById() throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask epic =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicForSubRemove", null));
    SubTask sub1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubRemove1", epic.getId(), null, null));
    SubTask sub2 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubRemove2", epic.getId(), null, null));

    manager.updateTask(
        new SubTaskUpdateDTO(
            sub2.getId(), // Update sub2 to DONE
            sub2.getTitle(),
            sub2.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));

    // Now remove sub2 (the DONE one), sub1 (NEW) remains
    manager.removeTaskById(sub2.getId());
    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();

    assertFalse(
        manager.getTask(sub2.getId()).isPresent(), "Removed subtask sub2 should not be found");
    assertTrue(
        updatedEpic.getSubtaskIds().contains(sub1.getId()), "Epic should still contain sub1");
    assertEquals(
        1, updatedEpic.getSubtaskIds().size(), "Epic should have one remaining subtask (sub1)");
    assertEquals(
        TaskStatus.NEW, updatedEpic.getStatus(), "Epic status should be NEW (as sub1 is NEW)");

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertFalse(
        newManager.getTask(sub2.getId()).isPresent(),
        "Removed subtask sub2 should not be in new manager instance");
    EpicTask loadedEpic = (EpicTask) newManager.getTask(epic.getId()).orElseThrow();
    assertEquals(1, loadedEpic.getSubtaskIds().size());
    assertTrue(loadedEpic.getSubtaskIds().contains(sub1.getId()));
    assertEquals(TaskStatus.NEW, loadedEpic.getStatus());
  }

  @Test
  @DisplayName("Should remove an EpicTask by ID along with all its SubTasks and persist")
  void testRemoveEpicTaskById()
      throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask epic =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicToRemoveFull", null));
    SubTask sub1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubOfEpicRemove1", epic.getId(), null, null));
    SubTask sub2 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubOfEpicRemove2", epic.getId(), null, null));

    Optional<Task> removed = manager.removeTaskById(epic.getId());
    assertTrue(removed.isPresent());
    assertFalse(manager.getTask(epic.getId()).isPresent());
    assertFalse(manager.getTask(sub1.getId()).isPresent());
    assertFalse(manager.getTask(sub2.getId()).isPresent());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertFalse(newManager.getTask(epic.getId()).isPresent());
    assertFalse(newManager.getTask(sub1.getId()).isPresent());
    assertFalse(newManager.getTask(sub2.getId()).isPresent());
  }

  @Test
  @DisplayName("Should remove all RegularTasks by type and persist")
  void testRemoveTasksByTypeRegularTask()
      throws ValidationException, TaskNotFoundException, OverlapException {
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("RegTypeRemove1", null, null));
    addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("RegTypeRemove2", null, null));
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicTypeKeep", null));

    manager.removeTasksByType(RegularTask.class);

    assertTrue(manager.getAllTasksByClass(RegularTask.class).isEmpty());
    assertFalse(manager.getAllTasksByClass(EpicTask.class).isEmpty());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertTrue(newManager.getAllTasksByClass(RegularTask.class).isEmpty());
    assertEquals(1, newManager.getAllTasksByClass(EpicTask.class).size());
    assertEquals(
        epic.getId(), newManager.getAllTasksByClass(EpicTask.class).iterator().next().getId());
  }

  @Test
  @DisplayName("Should remove all SubTasks by type, update Epic status to NEW, and persist")
  void testRemoveTasksByTypeSubTask()
      throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask epic =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicForSubRemoveType", null));
    SubTask sub1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubTypeRemove1", epic.getId(), null, null));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTO("SubTypeRemove2", epic.getId(), null, null));

    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            sub1.getTitle(),
            sub1.getDescription(),
            TaskStatus.DONE,
            epic.getId(),
            null,
            null));
    manager.removeTasksByType(SubTask.class);

    assertTrue(manager.getAllTasksByClass(SubTask.class).isEmpty());
    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertTrue(updatedEpic.getSubtaskIds().isEmpty());
    assertEquals(TaskStatus.NEW, updatedEpic.getStatus());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertTrue(newManager.getAllTasksByClass(SubTask.class).isEmpty());
    EpicTask loadedEpic = (EpicTask) newManager.getTask(epic.getId()).orElseThrow();
    assertTrue(loadedEpic.getSubtaskIds().isEmpty());
    assertEquals(TaskStatus.NEW, loadedEpic.getStatus());
  }

  @Test
  @DisplayName("Should remove all EpicTasks by type, their SubTasks, and persist")
  void testRemoveTasksByTypeEpicTask()
      throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask epic =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicTypeRemoveFull", null));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTO("SubOfTypeRemove1", epic.getId(), null, null));
    addAndRetrieveSubTask(
        createValidSubTaskCreationDTO("SubOfTypeRemove2", epic.getId(), null, null));

    manager.removeTasksByType(EpicTask.class);

    assertTrue(manager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertTrue(manager.getAllTasksByClass(SubTask.class).isEmpty());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertTrue(newManager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertTrue(newManager.getAllTasksByClass(SubTask.class).isEmpty());
  }

  @Test
  @DisplayName("Should throw UnsupportedOperationException when removing tasks by base Task type")
  void testRemoveTasksByTypeUnsupported() {
    assertThrows(UnsupportedOperationException.class, () -> manager.removeTasksByType(Task.class));
  }

  @Test
  @DisplayName("Should return empty Optional when removing a non-existent task by ID")
  void testRemoveTaskByNonExistentId()
      throws ValidationException, TaskNotFoundException, OverlapException {
    Optional<Task> removed = manager.removeTaskById(UUID.randomUUID());
    assertTrue(removed.isEmpty());
  }

  @Test
  @DisplayName(
      "Should verify EpicTask status is DONE after removing last incomplete SubTask and persist")
  void testRemoveLastIncompleteSubTaskVerifiesEpicDone()
      throws ValidationException, TaskNotFoundException, OverlapException {
    EpicTask epic = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("EpicDoneVerify", null));
    SubTask sub1 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubDoneVerify1", epic.getId(), null, null));
    SubTask sub2 =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO("SubDoneVerify2", epic.getId(), null, null));

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
            TaskStatus.IN_PROGRESS,
            epic.getId(),
            null,
            null));
    manager.removeTaskById(sub2.getId());
    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();

    assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    assertFalse(manager.getTask(sub2.getId()).isPresent());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    EpicTask loadedEpic = (EpicTask) newManager.getTask(epic.getId()).orElseThrow();
    assertEquals(TaskStatus.DONE, loadedEpic.getStatus());
    assertFalse(newManager.getTask(sub2.getId()).isPresent());
  }

  @Test
  @DisplayName("Updating a non-existent task should throw ValidationException")
  void testUpdateNonExistentTask() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            UUID.randomUUID(),
            VALID_TITLE_PREFIX + "NonExist",
            VALID_DESCRIPTION_PREFIX + "NonExist",
            TaskStatus.NEW,
            null,
            null);
    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName("History should initially be empty")
  void testInitialHistoryIsEmpty() {
    assertTrue(manager.getHistory().isEmpty());
  }

  @Test
  @DisplayName("Accessing a task should add it to the history")
  void testAddTaskToHistoryOnAccess() throws ValidationException, OverlapException {
    RegularTask regularTask =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("HistoryAccess", null, null));
    manager.getTask(regularTask.getId());
    Collection<Task> history = manager.getHistory();
    assertEquals(1, history.size());
    Task firstHistoryItem = history.iterator().next();
    assertEquals(regularTask.getId(), firstHistoryItem.getId());
  }

  @Test
  @DisplayName("History should not contain deleted tasks")
  void testHistoryShouldNotContainDeletedTasks()
      throws ValidationException, TaskNotFoundException, OverlapException {
    RegularTask regularTask =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("HistoryDelete", null, null));
    manager.getTask(regularTask.getId());
    manager.removeTaskById(regularTask.getId());
    Collection<Task> historyAfterDeletion = manager.getHistory();
    assertFalse(
        historyAfterDeletion.stream().anyMatch(t -> t.getId().equals(regularTask.getId())),
        "History should not contain the deleted task's ID.");
  }

  @Test
  @DisplayName("History should be updated in correct order of task access (LRU)")
  void testHistoryAccessOrder() throws ValidationException, OverlapException {
    RegularTask task1 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("HistOrder1", null, null));
    RegularTask task2 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("HistOrder2", null, null));
    RegularTask task3 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("HistOrder3", null, null));

    manager.getTask(task1.getId());
    manager.getTask(task2.getId());
    manager.getTask(task3.getId());
    manager.getTask(task1.getId());

    List<UUID> historyIds =
        manager.getHistory().stream().map(Task::getId).collect(Collectors.toList());
    assertEquals(3, historyIds.size());
    assertEquals(task2.getId(), historyIds.get(0));
    assertEquals(task3.getId(), historyIds.get(1));
    assertEquals(task1.getId(), historyIds.get(2));
  }

  @Test
  @DisplayName("File content should reflect added tasks")
  void testFileContent_AfterAddingTasks()
      throws ValidationException, IOException, TaskNotFoundException, OverlapException {
    RegularTask task1 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTO(
                "FileContent1", DEFAULT_START_TIME, DEFAULT_DURATION));
    EpicTask initialEpic =
        addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("FileContentEpic", null));
    SubTask subTaskContent =
        addAndRetrieveSubTask(
            createValidSubTaskCreationDTO(
                "FileContentSub", initialEpic.getId(), DEFAULT_START_TIME_2, DEFAULT_DURATION));

    EpicTask currentEpicState =
        (EpicTask)
            manager
                .getTask(initialEpic.getId())
                .orElseThrow(() -> new AssertionError("Epic task not found after adding subtask"));

    List<String> lines = Files.readAllLines(testDataFile);
    assertEquals(
        4,
        lines.size(),
        "File should contain header and 3 task lines. Actual: "
            + lines.size()
            + "\n"
            + String.join("\n", lines));

    String task1IdString = task1.getId().toString();
    String epicIdString = currentEpicState.getId().toString(); // Use current state
    String subTaskContentIdString = subTaskContent.getId().toString();

    assertTrue(
        lines.stream().anyMatch(line -> line.contains(task1IdString)),
        "Task1 ID not found in file");
    assertTrue(
        lines.stream().anyMatch(line -> line.contains(epicIdString)), "Epic1 ID not found in file");
    assertTrue(
        lines.stream()
            .anyMatch(line -> line.contains(subTaskContentIdString) && line.contains("SUBTASK")),
        "SubTaskContent line not found in file");

    Optional<String> epicLineOptional =
        lines.stream()
            .filter(line -> line.contains(epicIdString) && line.contains("EPIC"))
            .findFirst();
    assertTrue(epicLineOptional.isPresent(), "Epic line not found in CSV");

    assertFalse(currentEpicState.getSubtaskIds().isEmpty(), "Updated epic1 should have subtasks.");
    String actualSubTaskIdInEpicObject =
        currentEpicState.getSubtaskIds().iterator().next().toString();
    assertEquals(
        subTaskContentIdString,
        actualSubTaskIdInEpicObject,
        "The subtask ID in the fetched epic object is incorrect.");

    assertTrue(
        epicLineOptional.get().contains(actualSubTaskIdInEpicObject),
        "Subtask ID "
            + actualSubTaskIdInEpicObject
            + " not found in Epic's CSV entry: "
            + epicLineOptional.get());
  }

  @Test
  @DisplayName("Tasks should persist after adding and reinitializing manager")
  void testTasksPersist_AfterAddingAndReinitializingManager()
      throws ValidationException, OverlapException {
    RegularTask task1 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("PersistAdd1", null, null));
    EpicTask epic1 = addAndRetrieveEpicTask(createValidEpicTaskCreationDTO("PersistAddEpic", null));

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));

    assertTrue(newManager.getTask(task1.getId()).isPresent());
    assertEquals(task1.getTitle(), newManager.getTask(task1.getId()).get().getTitle());
    assertTrue(newManager.getTask(epic1.getId()).isPresent());
    assertEquals(epic1.getTitle(), newManager.getTask(epic1.getId()).get().getTitle());
    assertEquals(2, newManager.getAllTasks().size());
  }

  @Test
  @DisplayName("Tasks should persist after updating and reinitializing manager")
  void testTasksPersist_AfterUpdatingAndReinitializingManager()
      throws ValidationException, TaskNotFoundException, OverlapException {
    RegularTask task1 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTO("PersistUpdateOrig", null, null));
    String updatedTitle = VALID_TITLE_PREFIX + "PersistUpdateMod";
    manager.updateTask(
        new RegularTaskUpdateDTO(
            task1.getId(), updatedTitle, task1.getDescription(), TaskStatus.DONE, null, null));

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    Optional<Task> loadedTaskOpt = newManager.getTask(task1.getId());
    assertTrue(loadedTaskOpt.isPresent());
    assertEquals(updatedTitle, loadedTaskOpt.get().getTitle());
    assertEquals(TaskStatus.DONE, loadedTaskOpt.get().getStatus());
  }

  @Test
  @DisplayName("Tasks should persist after removing and reinitializing manager")
  void testTasksPersist_AfterRemovingAndReinitializingManager()
      throws ValidationException, TaskNotFoundException, OverlapException {
    RegularTask task1 =
        addAndRetrieveRegularTask(createValidRegularTaskCreationDTO("PersistRemove1", null, null));
    RegularTask task2 =
        addAndRetrieveRegularTask(
            createValidRegularTaskCreationDTO("PersistRemove2Keep", null, null));
    manager.removeTaskById(task1.getId());

    TaskManager newManager =
        new TaskManagerImpl(
            new FileBakedTaskRepository(testDataFile),
            new InMemoryHistoryManager(new InMemoryHistoryStore()));
    assertFalse(newManager.getTask(task1.getId()).isPresent());
    assertTrue(newManager.getTask(task2.getId()).isPresent());
    assertEquals(1, newManager.getAllTasks().size());
  }
}
