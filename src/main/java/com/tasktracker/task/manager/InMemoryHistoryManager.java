package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

/**
 * A manager that handles the history of tasks in memory with a defined limit. This implementation
 * interacts with a {@link HistoryRepository} to store and manage the task history. It ensures the
 * history does not exceed a specified maximum length.
 */
public class InMemoryHistoryManager implements HistoryManager {
  private final HistoryRepository historyStore;
  private final int historyLengthLimit;

  /**
   * Creates an instance of {@code InMemoryHistoryManager} to manage task history.
   *
   * @param historyStore the repository for storing task history; must not be {@code null}
   * @param historyLengthLimit the maximum number of tasks allowed in the history; must be at least
   *     1
   * @throws NullPointerException if {@code historyStore} is {@code null}
   * @throws IllegalArgumentException if {@code historyLengthLimit} is less than 1
   */
  public InMemoryHistoryManager(final HistoryRepository historyStore, final int historyLengthLimit)
      throws NullPointerException, IllegalArgumentException {
    Objects.requireNonNull(historyStore, "History Repository can't be null");
    this.historyStore = historyStore;
    this.historyLengthLimit = getValidatedHistoryLimit(historyLengthLimit);
  }

  /**
   * Validates the provided history length limit to ensure it is greater than or equal to 1.
   *
   * @param historyLengthLimit the maximum number of tasks allowed in the history
   * @return the validated history length limit
   * @throws IllegalArgumentException if the provided limit is less than 1
   */
  private int getValidatedHistoryLimit(int historyLengthLimit) {
    if (historyLengthLimit < 1) {
      throw new IllegalArgumentException("History length limit must be at least 1.");
    }
    return historyLengthLimit;
  }

  /**
   * Retrieves the complete history of tasks as a collection of {@link TaskView} objects. This
   * includes only tasks currently stored in the {@link HistoryRepository}.
   *
   * @return a collection of {@link TaskView} objects representing the task history
   */
  @Override
  public Collection<TaskView> getHistory() {
    return historyStore.getAll();
  }

  /**
   * Adds a task to the history. If the history reaches its maximum allowed size, the oldest task is
   * removed to make room for the new task. The task is saved in the history as a {@link TaskView}
   * object with the current timestamp.
   *
   * @param task the task to add to the history, must not be {@code null}
   * @return {@code true} if the task was successfully added, otherwise {@code false}
   * @throws NullPointerException if the provided task is {@code null}
   */
  @Override
  public boolean add(final Task task) throws NullPointerException {
    Objects.requireNonNull(task, "Task can't be null.");
    if (historyStore.size() == historyLengthLimit) {
      historyStore.pollFirst();
    }
    return historyStore.add(new TaskView(task.getId(), LocalDateTime.now()));
  }
}
