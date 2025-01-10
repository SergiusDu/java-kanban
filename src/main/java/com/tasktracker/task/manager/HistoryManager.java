package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryRepository;
import com.tasktracker.task.store.TaskRepository;
import java.util.Collection;

/**
 * The {@code HistoryManager} interface provides mechanisms for managing a history of tasks. It
 * allows retrieving the history and adding tasks to it based on their unique identifiers.
 */
public interface HistoryManager {

  /**
   * Retrieves the complete history of tasks as a collection of {@link Task} objects. Only tasks
   * that are still present in the {@link TaskRepository} are included in the result.
   *
   * @return a collection of {@link Task} objects present in the history and the repository
   */
  Collection<Task> getHistory();

  /**
   * Adds a task to the history by its unique identifier. If the history size exceeds the defined
   * history limit, the oldest task is removed to accommodate the new task. The task is stored in
   * the {@link HistoryRepository} as a {@link TaskView} object with the current timestamp.
   *
   * @param taskId the unique identifier of the {@link Task} to be added to the history
   * @return {@code true} if the task was successfully added, or {@code false} if the addition
   *     failed
   */
  boolean add(final int taskId);
}
