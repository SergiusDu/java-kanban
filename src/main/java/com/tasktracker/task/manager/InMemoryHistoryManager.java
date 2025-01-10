package com.tasktracker.task.manager;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.model.implementations.TaskView;
import com.tasktracker.task.store.HistoryRepository;
import com.tasktracker.task.store.TaskRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * A manager that handles the history of tasks in memory with a defined limit. This implementation
 * interacts with a {@link HistoryRepository} to store and manage the task history. It ensures the
 * history does not exceed a specified maximum length.
 */
public class InMemoryHistoryManager implements HistoryManager {
  private final HistoryRepository historyStore;
  private final TaskRepository taskStore;
  private final int historyLengthLimit;

  /**
   * Constructs an {@code InMemoryHistoryManager}.
   *
   * @param historyStore the repository used to store tasks
   * @param historyLimit the maximum number of tasks allowed in the history
   */
  public InMemoryHistoryManager(
      TaskRepository taskStore, final HistoryRepository historyStore, final int historyLimit) {
    this.taskStore = taskStore;
    this.historyStore = historyStore;
    this.historyLengthLimit = historyLimit;
  }

  /**
   * Retrieves the complete history of tasks as a collection of {@link Task} objects. Only tasks
   * that are still present in the {@link TaskRepository} are included in the result.
   *
   * @return a collection of {@link Task} objects present in the history and the repository
   */
  @Override
  public Collection<Task> getHistory() {
    return historyStore.getAll().stream()
        .map(taskView -> taskStore.getTaskById(taskView.getTaskId()))
        .flatMap(Optional::stream)
        .toList();
  }

  /**
   * Adds a task to the history by its unique identifier. If the history size exceeds the defined
   * history limit, the oldest task is removed to accommodate the new task. The task is stored in
   * the {@link HistoryRepository} as a {@link TaskView} object with the current timestamp.
   *
   * @param taskId the unique identifier of the {@link Task} to be added to the history
   * @return {@code true} if the task was successfully added, or {@code false} if the addition
   *     failed
   */
  @Override
  public boolean add(final int taskId) {
    if (historyStore.size() == historyLengthLimit) {
      historyStore.pollLast();
    }
    return historyStore.add(new TaskView(taskId, LocalDateTime.now()));
  }
}
