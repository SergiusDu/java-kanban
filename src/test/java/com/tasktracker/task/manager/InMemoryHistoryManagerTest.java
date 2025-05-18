package com.tasktracker.task.manager;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryStore;
import com.tasktracker.task.store.InMemoryHistoryStore;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryHistoryManagerTest {

  private static final String VALID_TITLE_PREFIX = "History Test Title ";
  private static final String VALID_DESCRIPTION_PREFIX = "History Test Description ";
  private static final LocalDateTime DEFAULT_CREATION_TIME = LocalDateTime.now().minusHours(2);
  private static final LocalDateTime DEFAULT_UPDATE_TIME = LocalDateTime.now().minusHours(1);

  private InMemoryHistoryManager historyManager;
  private HistoryStore historyStore;

  @BeforeEach
  void init() {
    historyStore = new InMemoryHistoryStore();
    historyManager = new InMemoryHistoryManager(historyStore);
  }

  private RegularTask createTask(String titleSuffix) throws ValidationException {
    return new RegularTask(
        UUID.randomUUID(),
        VALID_TITLE_PREFIX + titleSuffix,
        VALID_DESCRIPTION_PREFIX + titleSuffix,
        TaskStatus.NEW,
        DEFAULT_CREATION_TIME,
        DEFAULT_UPDATE_TIME,
        null,
        null);
  }

  @Test
  @DisplayName("Constructor should throw NullPointerException when historyStore is null")
  void constructor_NullHistoryStore_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> new InMemoryHistoryManager(null));
  }

  @Test
  @DisplayName("getHistory should return an empty history initially")
  void getHistory_Initially_ShouldBeEmpty() {
    Collection<TaskView> history = historyManager.getHistory();
    assertTrue(history.isEmpty());
  }

  @Test
  @DisplayName("put should throw NullPointerException when adding a null task")
  void put_NullTask_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> historyManager.put(null));
  }

  @Test
  @DisplayName("put should add a task to history and return Optional.empty if no previous task")
  void put_ValidTask_ShouldAddTaskToHistory() throws ValidationException {
    Task task1 = createTask("1");
    Optional<TaskView> previousViewOpt = historyManager.put(task1);

    assertFalse(
        previousViewOpt.isPresent(), "No previous TaskView should be returned for first add");

    Collection<TaskView> history = historyManager.getHistory();
    assertEquals(1, history.size());
    TaskView viewInHistory = history.iterator().next();
    assertEquals(task1.getId(), viewInHistory.getTaskId());
    assertNotNull(viewInHistory.getViewDateTime(), "ViewDateTime should be set");
  }

  @Test
  @DisplayName("put should maintain access order (LRU - most recent at the end)")
  void put_MultipleTasks_ShouldMaintainAccessOrder() throws ValidationException {
    Task task1 = createTask("Order1");
    Task task2 = createTask("Order2");
    Task task3 = createTask("Order3");

    historyManager.put(task1); // History: [T1]
    historyManager.put(task2); // History: [T1, T2]
    historyManager.put(task3); // History: [T1, T2, T3]

    List<UUID> historyIds =
        historyManager.getHistory().stream().map(TaskView::getTaskId).collect(Collectors.toList());

    assertEquals(3, historyIds.size());
    assertEquals(task1.getId(), historyIds.get(0));
    assertEquals(task2.getId(), historyIds.get(1));
    assertEquals(task3.getId(), historyIds.get(2));
  }

  @Test
  @DisplayName("put should move an existing task to the end if accessed again")
  void put_ExistingTaskAccessedAgain_ShouldMoveToEnd() throws ValidationException {
    Task task1 = createTask("Reorder1");
    Task task2 = createTask("Reorder2");
    Task task3 = createTask("Reorder3");

    historyManager.put(task1); // History: [T1]
    historyManager.put(task2); // History: [T1, T2]
    historyManager.put(task3); // History: [T1, T2, T3]
    historyManager.put(task1); // Access T1 again. History should be: [T2, T3, T1]

    List<UUID> historyIds =
        historyManager.getHistory().stream().map(TaskView::getTaskId).collect(Collectors.toList());

    assertEquals(3, historyIds.size(), "History size should remain 3");
    assertEquals(task2.getId(), historyIds.get(0), "T2 should now be first");
    assertEquals(task3.getId(), historyIds.get(1), "T3 should now be second");
    assertEquals(task1.getId(), historyIds.get(2), "T1 should now be last (most recent)");
  }

  @Test
  @DisplayName("getHistory should return an unmodifiable collection or a copy")
  void getHistory_ReturnedCollection_ShouldBeUnmodifiableOrCopy() throws ValidationException {
    Task task1 = createTask("Unmod1");
    historyManager.put(task1);
    Collection<TaskView> history = historyManager.getHistory();

    // Attempt to modify (this behavior depends on Collections.unmodifiableCollection used in store)
    // If it's truly unmodifiable, this will throw. If it's a copy, this won't affect the original.
    UUID dummyId = UUID.randomUUID();
    TaskView dummyView = new TaskView(dummyId, LocalDateTime.now());

    try {
      history.add(dummyView); // Should throw if unmodifiable
      // If it doesn't throw, check if the original history in the manager was affected
      assertEquals(
          1,
          historyManager.getHistory().size(),
          "Adding to the retrieved collection should not affect the manager's history if it's a copy.");
      assertFalse(
          historyManager.getHistory().stream().anyMatch(tv -> tv.getTaskId().equals(dummyId)),
          "Dummy ID should not be in manager's history.");
    } catch (UnsupportedOperationException e) {
      // This is the expected behavior if Collections.unmodifiableCollection is used.
      assertTrue(
          true, "UnsupportedOperationException was correctly thrown for unmodifiable collection.");
    }
  }

  @Test
  @DisplayName("remove should remove TaskView by ID and return it")
  void remove_ExistingId_ShouldRemoveAndReturnTaskView() throws ValidationException {
    Task task1 = createTask("Remove1");
    historyManager.put(task1);
    assertEquals(1, historyManager.getHistory().size());

    Optional<TaskView> removedViewOpt = historyManager.remove(task1.getId());

    assertTrue(removedViewOpt.isPresent());
    assertEquals(task1.getId(), removedViewOpt.get().getTaskId());
    assertEquals(0, historyManager.getHistory().size());
  }

  @Test
  @DisplayName("remove should return Optional.empty when removing a non-existent ID")
  void remove_NonExistentId_ShouldReturnEmptyOptional() {
    Optional<TaskView> removedViewOpt = historyManager.remove(UUID.randomUUID());
    assertTrue(removedViewOpt.isEmpty());
  }

  @Test
  @DisplayName("remove should throw NullPointerException if ID is null")
  void remove_NullId_ShouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> historyManager.remove(null));
  }

  @Test
  @DisplayName("Multiple operations: add, re-add, remove, check order and content")
  void multipleOperations_ComplexScenario_ShouldBehaveCorrectly() throws ValidationException {
    Task t1 = createTask("Complex1");
    Task t2 = createTask("Complex2");
    Task t3 = createTask("Complex3");
    Task t4 = createTask("Complex4");

    historyManager.put(t1); // [T1]
    historyManager.put(t2); // [T1, T2]
    historyManager.put(t3); // [T1, T2, T3]
    assertEquals(3, historyManager.getHistory().size());

    historyManager.put(t1); // [T2, T3, T1]
    List<UUID> idsAfterReAddT1 =
        historyManager.getHistory().stream().map(TaskView::getTaskId).collect(Collectors.toList());
    assertEquals(t2.getId(), idsAfterReAddT1.get(0));
    assertEquals(t3.getId(), idsAfterReAddT1.get(1));
    assertEquals(t1.getId(), idsAfterReAddT1.get(2));

    historyManager.remove(t3.getId()); // [T2, T1]
    List<UUID> idsAfterRemoveT3 =
        historyManager.getHistory().stream().map(TaskView::getTaskId).collect(Collectors.toList());
    assertEquals(2, idsAfterRemoveT3.size());
    assertEquals(t2.getId(), idsAfterRemoveT3.get(0));
    assertEquals(t1.getId(), idsAfterRemoveT3.get(1));

    historyManager.put(t4); // [T2, T1, T4]
    List<UUID> idsAfterAddT4 =
        historyManager.getHistory().stream().map(TaskView::getTaskId).collect(Collectors.toList());
    assertEquals(3, idsAfterAddT4.size());
    assertEquals(t2.getId(), idsAfterAddT4.get(0));
    assertEquals(t1.getId(), idsAfterAddT4.get(1));
    assertEquals(t4.getId(), idsAfterAddT4.get(2));

    historyManager.remove(UUID.randomUUID()); // Remove non-existent, should not change
    assertEquals(3, historyManager.getHistory().size());
    List<UUID> idsAfterRemoveNonExistent =
        historyManager.getHistory().stream().map(TaskView::getTaskId).collect(Collectors.toList());
    assertEquals(idsAfterAddT4, idsAfterRemoveNonExistent);
  }
}
