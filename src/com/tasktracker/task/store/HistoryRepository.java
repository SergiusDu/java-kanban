package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.Task;
import java.util.Collection;
import java.util.Optional;

/**
 * Repository interface for managing and retrieving tasks' history. Provides methods for adding,
 * retrieving, and removing tasks from history.
 */
public interface HistoryRepository {
  /**
   * Adds a task to the history repository.
   *
   * @param task the task to be added
   * @return {@code true} if the task was successfully added, {@code false} otherwise
   */
  boolean add(final Task task);

  /**
   * Retrieves all tasks stored in the history repository.
   *
   * @return a collection containing all tasks in the repository
   */
  Collection<Task> getAll();

  /**
   * Returns the number of tasks currently stored in the repository.
   *
   * @return the size of the repository
   */
  int size();

  /**
   * Retrieves and removes the first task from the repository, if one exists.
   *
   * @return an {@code Optional} containing the first task, or an empty {@code Optional} if the
   *     repository is empty
   */
  Optional<Task> pollFirst();
}
