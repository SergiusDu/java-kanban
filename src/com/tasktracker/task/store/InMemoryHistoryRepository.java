package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.Task;
import java.util.*;

/**
 * A repository implementation for managing task history in memory using a TreeSet. The tasks are
 * stored in a navigable, ordered collection to maintain a defined order.
 */
public class InMemoryHistoryRepository implements HistoryRepository {
  private final NavigableSet<Task> store = new TreeSet<>();

  /**
   * Adds the specified task to the history repository.
   *
   * @param task the task to be added
   * @return {@code true} if the task was successfully added, {@code false} otherwise
   */
  @Override
  public boolean add(final Task task) {
    return store.add(task);
  }

  /**
   * Retrieves all tasks stored in the history repository.
   *
   * @return an unmodifiable collection of all tasks in the repository
   */
  @Override
  public Collection<Task> getAll() {
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
   * Retrieves and removes the first task in the history repository, if present.
   *
   * @return an {@link Optional} containing the first task, or an empty {@link Optional} if the
   *     repository is empty
   */
  @Override
  public Optional<Task> pollFirst() {
    return Optional.ofNullable(store.pollFirst());
  }
}
