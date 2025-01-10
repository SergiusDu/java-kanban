package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.TaskView;
import java.util.Collection;
import java.util.Optional;

/**
 * Repository interface for managing and retrieving tasks' history. Provides methods for adding,
 * retrieving, and removing tasks from history.
 */
public interface HistoryRepository {
  /**
   * Adds a new task view to the history repository. If the task view is already present, it will
   * not be added again.
   *
   * @param taskView the task view to be added to the repository
   * @return {@code true} if the task view was successfully added, {@code false} if it was already
   *     present
   * @throws NullPointerException if the provided {@code taskView} is {@code null}
   */
  boolean add(TaskView taskView);

  /**
   * Retrieves all tasks stored in the history repository.
   *
   * @return a collection containing all tasks in the repository
   */
  Collection<TaskView> getAll();

  /**
   * Returns the number of tasks currently stored in the repository.
   *
   * @return the size of the repository
   */
  int size();

  /**
   * Removes and returns the first task from the history repository, if present.
   *
   * @return an {@code Optional} containing the first task if it exists, or an empty {@code
   *     Optional} if the repository is empty
   */
  Optional<TaskView> pollFirst();
}
