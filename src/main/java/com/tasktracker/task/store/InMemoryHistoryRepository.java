package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.TaskView;
import java.util.*;

/**
 * A repository implementation for managing task history in memory using a TreeSet. The tasks are
 * stored in a navigable, ordered collection to maintain a defined order.
 */
public class InMemoryHistoryRepository implements HistoryRepository {
  private static final int INITIAL_CAPACITY = 16;
  private static final float LOAD_FACTOR = 0.75f;
  private final LinkedHashMap<Integer, TaskView> store =
      new LinkedHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, true);

  /**
   * Adds a new task view to the history repository, replacing any existing task view with the same
   * ID.
   *
   * @param taskView the task view to be added to the repository
   * @return an {@link Optional} containing the previous {@link TaskView} if one was replaced, or an
   *     empty {@link Optional} if no task view with the same ID existed
   * @throws NullPointerException if the provided {@code taskView} is {@code null}
   */
  @Override
  public Optional<TaskView> put(final TaskView taskView) {
    return Optional.ofNullable(store.put(taskView.getTaskId(), taskView));
  }

  /**
   * Retrieves all task views stored in the history repository as a collection of map entries. Each
   * entry consists of the task ID as the key and its corresponding {@link TaskView} as the value.
   *
   * @return an unmodifiable collection of map entries containing all task views in the repository
   */
  @Override
  public Collection<TaskView> getAll() {
    return Collections.unmodifiableCollection(store.values());
  }

  /**
   * Removes a task view from the history repository by its ID, if it exists.
   *
   * @param id the ID of the task view to be removed
   * @return an {@link Optional} containing the removed {@link TaskView} if found, or an empty
   *     {@link Optional} if no task view with the given ID exists
   */
  @Override
  public Optional<TaskView> remove(int id) {
    return Optional.ofNullable(store.remove(id));
  }
}
