package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.HistoryRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * A manager that handles the history of tasks in memory with a defined limit. This implementation
 * interacts with a {@link HistoryRepository} to store and manage the task history. It ensures the
 * history does not exceed a specified maximum length.
 */
public class InMemoryHistoryManager implements HistoryManager {
  private final HistoryRepository store;
  private final int historyLengthLimit;

  /**
   * Constructs an {@code InMemoryHistoryManager}.
   *
   * @param store the repository used to store tasks
   * @param historyLimit the maximum number of tasks allowed in the history
   */
  public InMemoryHistoryManager(final HistoryRepository store, final int historyLimit) {
    this.store = store;
    this.historyLengthLimit = historyLimit;
  }

  /**
   * Retrieves the complete history of tasks as an unmodifiable collection.
   *
   * @return a collection containing all tasks in the history
   */
  @Override
  public Collection<Task> getHistory() {
    return Collections.unmodifiableCollection(store.getAll());
  }

  /**
   * Adds a task to the history. If the history exceeds the maximum length, the oldest task is
   * removed.
   *
   * @param task the task to be added to the history
   * @return an {@code Optional} containing the removed task if the history limit was exceeded;
   *     otherwise, an empty {@code Optional}
   */
  @Override
  public Optional<Task> add(final Task task) {
    boolean isTaskReplaced = store.add(task);
    if (!isTaskReplaced) return Optional.empty();
    Optional<Task> removedTask = Optional.empty();
    if (store.size() > historyLengthLimit) {
      removedTask = store.pollFirst();
    }
    return removedTask;
  }
}
