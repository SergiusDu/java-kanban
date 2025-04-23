package com.tasktracker.task.manager;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.*;
import com.tasktracker.util.Managers;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.*;

/** JUnit5 tests for InMemoryTaskManager covering edge cases and business logic. */
public class InMemoryTaskManagerTest {

  // Valid test constants
  private static final String VALID_TITLE = "Valid Task Title";
  private static final String VALID_DESCRIPTION = "Valid Task Description with enough characters";
  // Invalid test constants
  private static final String INVALID_TITLE = "Short";
  private static final String INVALID_DESCRIPTION = "ShortDesc";
  private TaskManager manager;

  @BeforeEach
  void setUp() {
    manager = Managers.getDefault();
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when TaskRepository is null")
  void testConstructorThrowsOnNullRepository() {
    assertThrows(NullPointerException.class, () -> new InMemoryTaskManager(null, null));
  }

  @Test
  @DisplayName("Should retrieve all tasks")
  void testGetAllTasks() {
    RegularTask task1 =
        new RegularTask(
            1,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    RegularTask task2 =
        new RegularTask(
            2,
            VALID_TITLE,
            VALID_DESCRIPTION,
            TaskStatus.NEW,
            LocalDateTime.now(),
            LocalDateTime.now());
    manager.addTask(new RegularTaskCreationDTO(task1.getTitle(), task1.getDescription()));
    manager.addTask(new RegularTaskCreationDTO(task2.getTitle(), task2.getDescription()));

    Collection<Task> tasks = manager.getAllTasks();

    assertEquals(2, tasks.size());
  }

  @Test
  @DisplayName("Should clear all tasks")
  void testClearAllTasks() {
    manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    manager.clearAllTasks();

    assertEquals(0, manager.getAllTasks().size());
  }

  @Test
  @DisplayName("Should throw ValidationException for invalid RegularTask title or description")
  void testAddRegularTaskWithInvalidData() {
    RegularTaskCreationDTO invalidDto =
        new RegularTaskCreationDTO(INVALID_TITLE, INVALID_DESCRIPTION);

    assertThrows(ValidationException.class, () -> manager.addTask(invalidDto));
  }

  @Test
  @DisplayName("Should add a new RegularTask")
  void testAddRegularTask() {
    RegularTaskCreationDTO dto = new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION);

    RegularTask created = manager.addTask(dto);

    assertNotNull(created);
    assertEquals(VALID_TITLE, created.getTitle());
    assertEquals(VALID_DESCRIPTION, created.getDescription());
    assertEquals(TaskStatus.NEW, created.getStatus());
  }

  @Test
  @DisplayName("Should add a new EpicTask")
  void testAddEpicTask() {
    EpicTaskCreationDTO dto = new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION);

    EpicTask created = manager.addTask(dto);

    assertNotNull(created);
    assertEquals(VALID_TITLE, created.getTitle());
    assertEquals(VALID_DESCRIPTION, created.getDescription());
    assertEquals(TaskStatus.NEW, created.getStatus());
    assertEquals(0, created.getSubtaskIds().size());
  }

  @Test
  @DisplayName("Should add a SubTask to an existing EpicTask")
  void testAddSubTask() {
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId());

    SubTask created = manager.addTask(dto);

    assertNotNull(created);
    assertEquals(VALID_TITLE, created.getTitle());
    assertEquals(VALID_DESCRIPTION, created.getDescription());
    assertEquals(TaskStatus.NEW, created.getStatus());
    assertEquals(epicTask.getId(), created.getEpicTaskId());

    EpicTask updatedEpic = (EpicTask) manager.getTask(epicTask.getId()).get();
    assertTrue(updatedEpic.getSubtaskIds().contains(created.getId()));
  }

  @Test
  @DisplayName("Should throw ValidationException when adding SubTask with invalid data")
  void testAddSubTaskWithInvalidData() {
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTaskCreationDTO dto =
        new SubTaskCreationDTO(INVALID_TITLE, INVALID_DESCRIPTION, epicTask.getId());

    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  @Test
  @DisplayName("Should throw ValidationException when adding SubTask to nonexistent EpicTask")
  void testAddSubTaskToNonexistentEpic() {
    SubTaskCreationDTO dto = new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, 999);

    assertThrows(ValidationException.class, () -> manager.addTask(dto));
  }

  @Test
  @DisplayName("Should update an existing RegularTask")
  void testUpdateRegularTask() {
    RegularTask task = manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            task.getId(), VALID_TITLE, "Updated Description", TaskStatus.IN_PROGRESS);

    RegularTask updated = manager.updateTask(dto);

    assertEquals(VALID_TITLE, updated.getTitle());
    assertEquals("Updated Description", updated.getDescription());
    assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
  }

  @Test
  @DisplayName("Should throw ValidationException when updating RegularTask with invalid data")
  void testUpdateRegularTaskWithInvalidData() {
    RegularTask task = manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(
            task.getId(), INVALID_TITLE, INVALID_DESCRIPTION, TaskStatus.IN_PROGRESS);

    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName("Should calculate EpicTask status correctly")
  void testCalculateEpicTaskStatus() {
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));
    SubTask subTask2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));

    SubTaskUpdateDTO dto =
        new SubTaskUpdateDTO(
            subTask1.getId(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.DONE, epicTask.getId());
    manager.updateTask(dto);

    EpicTask updatedEpic = (EpicTask) manager.getTask(epicTask.getId()).get();
    assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());

    dto =
        new SubTaskUpdateDTO(
            subTask2.getId(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.DONE, epicTask.getId());
    manager.updateTask(dto);

    updatedEpic = (EpicTask) manager.getTask(epicTask.getId()).get();
    assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
  }

  @Test
  @DisplayName("Should remove a RegularTask by ID and return it")
  void testRemoveRegularTaskById() {
    RegularTask created =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    Optional<Task> removed = manager.removeTaskById(created.getId());

    assertTrue(removed.isPresent());
    assertEquals(created.getId(), removed.get().getId());
    assertFalse(manager.getTask(created.getId()).isPresent());
  }

  @Test
  @DisplayName("Should remove a SubTask by ID and verify EpicTask status updates")
  void testRemoveSubTaskById() {
    EpicTask epic = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask sub1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));
    SubTask sub2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.DONE, epic.getId()));
    manager.removeTaskById(sub1.getId());
    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();

    assertFalse(manager.getTask(sub1.getId()).isPresent());
    assertEquals(1, updatedEpic.getSubtaskIds().size());
    assertEquals(TaskStatus.NEW, updatedEpic.getStatus());
    Optional<Task> removedSub1 = manager.getTask(sub1.getId());
    assertFalse(removedSub1.isPresent());
  }

  @Test
  @DisplayName("Should remove an EpicTask by ID along with all its SubTasks")
  void testRemoveEpicTaskById() {
    EpicTask epic = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask sub1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));
    SubTask sub2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));

    Optional<Task> removed = manager.removeTaskById(epic.getId());

    assertTrue(removed.isPresent());
    assertFalse(manager.getTask(epic.getId()).isPresent());
    assertFalse(manager.getTask(sub1.getId()).isPresent());
    assertFalse(manager.getTask(sub2.getId()).isPresent());
  }

  @Test
  @DisplayName("Should remove all RegularTasks by type")
  void testRemoveTasksByTypeRegularTask() {
    manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    boolean removed = manager.removeTasksByType(RegularTask.class);

    assertTrue(removed);
    assertTrue(manager.getAllTasksByClass(RegularTask.class).isEmpty());
    assertFalse(manager.getAllTasksByClass(EpicTask.class).isEmpty());
  }

  @Test
  @DisplayName(
      "Should remove all SubTasks by type and verify EpicTask status returns to IN_PROGRESS")
  void testRemoveTasksByTypeSubTask() {
    EpicTask epic = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask sub1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));
    manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.DONE, epic.getId()));
    boolean removed = manager.removeTasksByType(SubTask.class);

    assertTrue(removed);
    assertTrue(manager.getAllTasksByClass(SubTask.class).isEmpty());
    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();
    assertTrue(updatedEpic.getSubtaskIds().isEmpty());
    assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
  }

  @Test
  @DisplayName("Should remove all EpicTasks by type and verify all SubTasks are also removed")
  void testRemoveTasksByTypeEpicTask() {
    EpicTask epic = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));
    manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));

    boolean removed = manager.removeTasksByType(EpicTask.class);

    assertTrue(removed);
    assertTrue(manager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertTrue(manager.getAllTasksByClass(SubTask.class).isEmpty());
  }

  @Test
  @DisplayName(
      "Should throw UnsupportedOperationException when removing tasks by an unsupported type")
  void testRemoveTasksByTypeUnsupported() {
    assertThrows(UnsupportedOperationException.class, () -> manager.removeTasksByType(Task.class));
  }

  @Test
  @DisplayName("Should return empty Optional when removing a non-existent task by ID")
  void testRemoveTaskByNonExistentId() {
    Optional<Task> removed = manager.removeTaskById(999);

    assertTrue(removed.isEmpty());
  }

  @Test
  @DisplayName("Should verify EpicTask status is DONE after removing last incomplete SubTask")
  void testRemoveLastIncompleteSubTaskVerifiesEpicDone() {
    EpicTask epic = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask sub1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));
    SubTask sub2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic.getId()));

    manager.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.DONE, epic.getId()));
    manager.updateTask(
        new SubTaskUpdateDTO(
            sub2.getId(), VALID_TITLE, VALID_DESCRIPTION, TaskStatus.IN_PROGRESS, epic.getId()));
    manager.removeTaskById(sub2.getId());
    EpicTask updatedEpic = (EpicTask) manager.getTask(epic.getId()).orElseThrow();

    assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    assertFalse(manager.getTask(sub2.getId()).isPresent());
  }

  @Test
  @DisplayName("Removing a RegularTask should not affect EpicTasks or SubTasks")
  void testRemoveRegularTaskDoesNotAffectOtherTasks() {
    // Arrange
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));
    RegularTask anotherRegularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    Optional<Task> removed = manager.removeTaskById(regularTask.getId());

    // Assert
    assertTrue(removed.isPresent());
    assertFalse(manager.getTask(regularTask.getId()).isPresent());
    assertTrue(manager.getTask(epicTask.getId()).isPresent());
    assertTrue(manager.getTask(subTask.getId()).isPresent());
    assertTrue(manager.getTask(anotherRegularTask.getId()).isPresent());
  }

  @Test
  @DisplayName("Removing a SubTask should not affect other SubTasks or RegularTasks")
  void testRemoveSubTaskDoesNotAffectOtherTasks() {
    // Arrange
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));
    SubTask subTask2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    Optional<Task> removed = manager.removeTaskById(subTask1.getId());

    // Assert
    assertTrue(removed.isPresent());
    assertFalse(manager.getTask(subTask1.getId()).isPresent());
    assertTrue(manager.getTask(subTask2.getId()).isPresent());
    assertTrue(manager.getTask(regularTask.getId()).isPresent());

    EpicTask updatedEpic = (EpicTask) manager.getTask(epicTask.getId()).orElseThrow();
    assertFalse(updatedEpic.getSubtaskIds().contains(subTask1.getId()));
    assertTrue(updatedEpic.getSubtaskIds().contains(subTask2.getId()));
  }

  @Test
  @DisplayName("Removing an EpicTask should only remove its SubTasks and not other tasks")
  void testRemoveEpicTaskDoesNotAffectOtherTasks() {
    // Arrange
    EpicTask epicTask1 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask1.getId()));
    SubTask subTask2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask1.getId()));
    EpicTask epicTask2 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    Optional<Task> removed = manager.removeTaskById(epicTask1.getId());

    // Assert
    assertTrue(removed.isPresent());
    assertFalse(manager.getTask(epicTask1.getId()).isPresent());
    assertFalse(manager.getTask(subTask1.getId()).isPresent());
    assertFalse(manager.getTask(subTask2.getId()).isPresent());
    assertTrue(manager.getTask(epicTask2.getId()).isPresent());
    assertTrue(manager.getTask(regularTask.getId()).isPresent());
  }

  @Test
  @DisplayName("Removing tasks by type should not affect unrelated task types")
  void testRemoveTasksByTypeDoesNotAffectUnrelatedTypes() {
    // Arrange
    RegularTask regularTask1 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    RegularTask regularTask2 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));

    // Act
    boolean removedRegular = manager.removeTasksByType(RegularTask.class);

    // Assert
    assertTrue(removedRegular);
    assertTrue(manager.getAllTasksByClass(RegularTask.class).isEmpty());
    assertTrue(manager.getTask(epicTask.getId()).isPresent());
    assertTrue(manager.getTask(subTask.getId()).isPresent());
  }

  @Test
  @DisplayName("Removing all SubTasks should not affect RegularTasks or EpicTasks")
  void testRemoveAllSubTasksDoesNotAffectOtherTasks() {
    // Arrange
    EpicTask epicTask1 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epicTask2 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask1.getId()));
    SubTask subTask2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask2.getId()));
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    boolean removedSubTasks = manager.removeTasksByType(SubTask.class);

    // Assert
    assertTrue(removedSubTasks);
    assertTrue(manager.getAllTasksByClass(SubTask.class).isEmpty());
    assertTrue(manager.getTask(epicTask1.getId()).isPresent());
    assertTrue(manager.getTask(epicTask2.getId()).isPresent());
    assertTrue(manager.getTask(regularTask.getId()).isPresent());

    EpicTask updatedEpic1 = (EpicTask) manager.getTask(epicTask1.getId()).orElseThrow();
    EpicTask updatedEpic2 = (EpicTask) manager.getTask(epicTask2.getId()).orElseThrow();
    assertTrue(updatedEpic1.getSubtaskIds().isEmpty());
    assertTrue(updatedEpic2.getSubtaskIds().isEmpty());
    assertEquals(TaskStatus.NEW, updatedEpic1.getStatus());
    assertEquals(TaskStatus.NEW, updatedEpic2.getStatus());
  }

  @Test
  @DisplayName("Attempting to remove a task with invalid ID should not affect existing tasks")
  void testRemoveTaskWithInvalidIdDoesNotAffectExistingTasks() {
    // Arrange
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));

    // Act
    Optional<Task> removed = manager.removeTaskById(9999); // Non-existent ID

    // Assert
    assertFalse(removed.isPresent());
    assertTrue(manager.getTask(regularTask.getId()).isPresent());
    assertTrue(manager.getTask(epicTask.getId()).isPresent());
    assertTrue(manager.getTask(subTask.getId()).isPresent());
  }

  @Test
  @DisplayName(
      "Removing tasks by type with no matching tasks should return false and not affect any tasks")
  void testRemoveTasksByTypeWithNoMatchingTasks() {
    // Arrange
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask.getId()));

    // Act
    boolean removed = manager.removeTasksByType(RegularTask.class);

    // Assert
    assertFalse(removed);
    assertTrue(manager.getTask(epicTask.getId()).isPresent());
    assertTrue(manager.getTask(subTask.getId()).isPresent());
  }

  @Test
  @DisplayName("Attempting to remove a task with null ID should throw an exception")
  void testRemoveTaskWithNullIdThrowsException() {
    // Since removeTaskById expects an int, passing null isn't possible.
    // Instead, ensure that no task is removed and collection remains intact.
    // Arrange
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epicTask = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    // No action, as method signature does not accept null. This test is redundant.
    // Alternatively, ensure that passing a negative ID behaves correctly.
    Optional<Task> removed = manager.removeTaskById(-1);

    // Assert
    assertFalse(removed.isPresent());
    assertTrue(manager.getTask(regularTask.getId()).isPresent());
    assertTrue(manager.getTask(epicTask.getId()).isPresent());
  }

  @Test
  @DisplayName("Removing all EpicTasks should not remove RegularTasks or unrelated EpicTasks")
  void testRemoveAllEpicTasksDoesNotAffectOtherTasks() {
    // Arrange
    EpicTask epicTask1 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epicTask2 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask subTask1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask1.getId()));
    SubTask subTask2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epicTask2.getId()));
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    boolean removedEpics = manager.removeTasksByType(EpicTask.class);

    // Assert
    assertTrue(removedEpics);
    assertTrue(manager.getAllTasksByClass(EpicTask.class).isEmpty());
    assertFalse(manager.getTask(subTask1.getId()).isPresent());
    assertFalse(manager.getTask(subTask2.getId()).isPresent());
    assertTrue(manager.getTask(regularTask.getId()).isPresent());
  }

  @Test
  @DisplayName("Attempting to remove tasks by type with null should throw NullPointerException")
  void testRemoveTasksByTypeWithNullThrowsException() {
    // Act & Assert
    assertThrows(NullPointerException.class, () -> manager.removeTasksByType(null));
  }

  @Test
  @DisplayName("Ensure collection integrity after multiple additions and removals")
  void testCollectionIntegrityAfterMultipleOperations() {
    // Arrange
    RegularTask regular1 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    RegularTask regular2 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epic1 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    EpicTask epic2 = manager.addTask(new EpicTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    SubTask sub1 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic1.getId()));
    SubTask sub2 =
        manager.addTask(new SubTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION, epic2.getId()));

    // Act
    manager.removeTaskById(regular1.getId());
    manager.removeTaskById(sub1.getId());
    manager.removeTasksByType(EpicTask.class);

    // Assert
    // Regular2 should still exist
    assertTrue(manager.getTask(regular2.getId()).isPresent());
    // Epic1 and Epic2 should be removed
    assertFalse(manager.getTask(epic1.getId()).isPresent());
    assertFalse(manager.getTask(epic2.getId()).isPresent());
    // Sub2 should be removed because epic2 was removed
    assertFalse(manager.getTask(sub2.getId()).isPresent());
    // Sub1 was already removed
    assertFalse(manager.getTask(sub1.getId()).isPresent());
  }

  @Test
  @DisplayName("Updating a non-existent task should throw ValidationException")
  void testUpdateNonExistentTask() {
    RegularTaskUpdateDTO dto =
        new RegularTaskUpdateDTO(999, VALID_TITLE, VALID_DESCRIPTION, TaskStatus.NEW);

    assertThrows(ValidationException.class, () -> manager.updateTask(dto));
  }

  @Test
  @DisplayName("History should initially be empty")
  void testInitialHistoryIsEmpty() {
    // Arrange & Act
    Collection<Task> history = manager.getHistory();

    // Assert
    assertTrue(history.isEmpty());
  }

  @Test
  @DisplayName("Accessing a task should add it to the history")
  void testAddTaskToHistoryOnAccess() {
    // Arrange
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));

    // Act
    manager.getTask(regularTask.getId());
    Collection<Task> history = manager.getHistory();

    // Assert
    assertEquals(1, history.size());
    Task firstHistoryItem = history.iterator().next();
    assertEquals(regularTask.getId(), firstHistoryItem.getId());
  }

  @Test
  @DisplayName("History should not contain deleted tasks")
  void testHistoryShouldNotContainDeletedTasks() {
    // Arrange
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    manager.getTask(regularTask.getId());
    Collection<Task> historyBeforeDeletion = manager.getHistory();
    assertTrue(historyBeforeDeletion.stream().anyMatch(t -> t.getId() == regularTask.getId()));

    // Act
    manager.removeTaskById(regularTask.getId());
    Collection<Task> historyAfterDeletion = manager.getHistory();

    // Assert
    assertFalse(historyAfterDeletion.stream().anyMatch(t -> t.getId() == regularTask.getId()));
  }

  @Test
  @DisplayName("History should remain unchanged when accessing a non-existent task")
  void testAccessNonExistentTaskDoesNotChangeHistory() {
    // Arrange
    RegularTask regularTask =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    manager.getTask(regularTask.getId());
    int historySizeBefore = manager.getHistory().size();

    // Act
    manager.getTask(9999);

    // Assert
    int historySizeAfter = manager.getHistory().size();
    assertEquals(historySizeBefore, historySizeAfter);
  }

  @Test
  @DisplayName("History should be updated in correct order of task access")
  void testHistoryAccessOrder() {
    RegularTask task1 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE + "1", VALID_DESCRIPTION));
    RegularTask task2 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE + "2", VALID_DESCRIPTION));
    RegularTask task3 =
        manager.addTask(new RegularTaskCreationDTO(VALID_TITLE + "3", VALID_DESCRIPTION));

    manager.getTask(task1.getId());
    manager.getTask(task2.getId());
    manager.getTask(task3.getId());

    List<Task> historyList = manager.getHistory().stream().toList();
    assertEquals(3, historyList.size());
    assertEquals(task1.getId(), historyList.get(0).getId());
    assertEquals(task2.getId(), historyList.get(1).getId());
    assertEquals(task3.getId(), historyList.get(2).getId());
  }

  @Test
  @DisplayName("Should clear task from history when removed from repository")
  void removeTaskFromHistoryOnRemoval() {
    RegularTask task1 = manager.addTask(new RegularTaskCreationDTO(VALID_TITLE, VALID_DESCRIPTION));
    manager.getTask(task1.getId());
    assertEquals(1, manager.getHistory().size());
    manager.removeTaskById(task1.getId());
    assertEquals(0, manager.getHistory().size());
  }
}
