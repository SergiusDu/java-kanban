package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryStore;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * The {@code HistoryManager} interface provides mechanisms for managing a history of tasks. It
 * allows retrieving the history and adding tasks to it based on their unique identifiers.
 */
public interface HistoryManager {

  /**
   * Retrieves the complete history of tasks as a collection of {@link TaskView} objects. This
   * includes only tasks currently stored in the {@link HistoryStore}.
   *
   * @return a collection of {@link TaskView} objects representing the task history
   */
  Collection<TaskView> getHistory();

  /**
   * Adds a task to the history. If the history reaches its maximum allowed size, the oldest task is
   * removed to make room for the new task. The task is saved in the history as a {@link TaskView}
   * object with the current timestamp.
   *
   * @param task the task to add to the history, must not be {@code null}
   * @return {@code true} if the task was successfully added, otherwise {@code false}
   */
  Optional<TaskView> put(final Task task);

  /**
   * Removes the task view associated with the specified task ID from the history repository.
   *
   * @param id the ID of the task to be removed
   * @return an {@link Optional} containing the removed {@link TaskView} if it existed; otherwise,
   *     an empty {@link Optional}
   */
  Optional<TaskView> remove(UUID id);
}
