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
   * Adds a new task view to the history repository, replacing any existing task view with the same
   * ID.
   *
   * @param taskView the task view to be added to the repository; must not be {@code null}
   * @return an {@link Optional} containing the previous {@link TaskView} if one was replaced, or an
   *     empty {@link Optional} if no task view with the same ID existed
   */
  Optional<TaskView> put(TaskView taskView);

  /**
   * Retrieves all task views stored in the history repository as a collection of map entries. Each
   * entry consists of the task ID as the key and its corresponding {@link TaskView} as the value.
   *
   * @return an unmodifiable collection of map entries containing all task views in the repository
   */
  Collection<TaskView> getAll();

  Optional<TaskView> remove(int id);
}
