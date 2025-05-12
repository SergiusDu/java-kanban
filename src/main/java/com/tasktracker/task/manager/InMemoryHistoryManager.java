package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryStore;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A manager implementation that handles task history in memory. It uses a {@link HistoryStore} for
 * storing and managing task views, ensuring efficient history management.
 */
public class InMemoryHistoryManager implements HistoryManager {
  private final HistoryStore historyStore;

  /**
   * Constructs an {@code InMemoryHistoryManager} with the specified {@link HistoryStore}. Ensures
   * the provided history repository is not null.
   *
   * @param historyStore the {@link HistoryStore} used for managing the task history; must not be
   *     {@code null}
   */
  public InMemoryHistoryManager(final HistoryStore historyStore) {
    Objects.requireNonNull(historyStore, "History Repository can't be null");
    this.historyStore = historyStore;
  }

  /**
   * Retrieves the complete history of tasks as a collection of {@link TaskView} objects. This
   * includes only tasks currently stored in the {@link HistoryStore}.
   *
   * @return a collection of {@link TaskView} objects representing the task history
   */
  @Override
  public Collection<TaskView> getHistory() {
    return historyStore.getAll();
  }

  /**
   * Adds the provided task to the history as a {@link TaskView}. The task's view is saved with the
   * current timestamp. If a task with the same ID already exists in the history, it will be
   * replaced.
   *
   * @param task the task to be added to the history, must not be {@code null}
   * @return an {@link Optional} containing the previous {@link TaskView}, if a task with the same
   *     ID was replaced; otherwise, an empty {@link Optional}
   */
  @Override
  public Optional<TaskView> put(final Task task) {
    Objects.requireNonNull(task, "Task can't be null.");
    return historyStore.put(new TaskView(task.getId(), LocalDateTime.now()));
  }

  /**
   * Removes the task view associated with the specified task ID from the history repository.
   *
   * @param id the ID of the task to be removed
   * @return an {@link Optional} containing the removed {@link TaskView} if it existed; otherwise,
   *     an empty {@link Optional}
   */
  @Override
  public Optional<TaskView> remove(UUID id) {
    return historyStore.remove(id);
  }
}
