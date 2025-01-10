package com.tasktracker.task.store;

import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.task.model.implementations.TaskView;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryHistoryRepositoryTest {

  InMemoryHistoryRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryHistoryRepository();
  }

  @Test
  void testAddShouldReturnTrueForNewTaskView() {
    TaskView view = new TaskView(1, LocalDateTime.now());
    assertTrue(repository.add(view));
  }

  @Test
  void testAddShouldReturnFalseForDuplicateTaskView() {
    TaskView view = new TaskView(2, LocalDateTime.now());
    repository.add(view);
    assertFalse(repository.add(view));
  }

  @Test
  void testGetAllShouldReturnUnmodifiableCollection() {
    TaskView view = new TaskView(3, LocalDateTime.now());
    repository.add(view);
    Collection<TaskView> all = repository.getAll();
    assertThrows(
        UnsupportedOperationException.class, () -> all.add(new TaskView(4, LocalDateTime.now())));
  }

  @Test
  void testSizeShouldReturnCorrectCount() {
    repository.add(new TaskView(5, LocalDateTime.now()));
    repository.add(new TaskView(6, LocalDateTime.now()));
    assertEquals(2, repository.size());
  }

  @Test
  void testPollFirstShouldReturnAndRemoveFirst() {
    LocalDateTime now = LocalDateTime.now();
    TaskView early = new TaskView(7, now.minusSeconds(10));
    TaskView later = new TaskView(8, now);
    repository.add(later);
    repository.add(early);
    Optional<TaskView> first = repository.pollFirst();
    assertTrue(first.isPresent());
    assertEquals(early, first.get());
    assertEquals(1, repository.size());
  }

  @Test
  void testPollFirstShouldReturnEmptyWhenEmpty() {
    assertTrue(repository.pollFirst().isEmpty());
  }
}
