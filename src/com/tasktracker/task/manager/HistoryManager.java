package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import java.util.Collection;
import java.util.Optional;

/**
 * The {@code HistoryManager} interface defines methods for managing a history of tasks. It provides
 * functionality to retrieve and add tasks to the history.
 */
public interface HistoryManager {

  /**
   * Retrieves the history of tasks.
   *
   * @return a collection of {@link Task} objects representing the task history.
   */
  Collection<Task> getHistory();

  /**
   * Adds a task to the history.
   *
   * @param task the {@link Task} to be added to the history.
   */
  Optional<Task> add(final Task task);
}
