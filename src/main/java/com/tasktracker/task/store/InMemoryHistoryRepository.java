package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.TaskView;
import java.util.*;

/**
 * A repository implementation for managing task history in memory using a TreeSet. The tasks are
 * stored in a navigable, ordered collection to maintain a defined order.
 */
public class InMemoryHistoryRepository implements HistoryRepository {
  private final NavigableSet<TaskView> store = new TreeSet<>();

  /**
   * Adds a new task view to the history repository. If the task view is already present, it will
   * not be added again.
   *
   * @param taskView the task view to be added to the repository
   * @return {@code true} if the task view was successfully added, {@code false} if it was already
   *     present
   * @throws NullPointerException if the provided {@code taskView} is {@code null}
   */
  @Override
  public boolean add(final TaskView taskView) {
    return store.add(taskView);
  }

  /**
   * Retrieves all tasks stored in the history repository.
   *
   * @return an unmodifiable collection of all tasks in the repository
   */
  @Override
  public Collection<TaskView> getAll() {
    return Collections.unmodifiableCollection(store);
  }

  /**
   * Returns the number of tasks currently in the history repository.
   *
   * @return the size of the task repository
   */
  @Override
  public int size() {
    return store.size();
  }

  /**
   * Removes and returns the first task in the history repository if it exists.
   *
   * @return an {@code Optional} containing the first task if present, otherwise an empty {@code
   *     Optional}
   */
  public Optional<TaskView> pollFirst() {
    return Optional.ofNullable(store.pollFirst());
  }
}
