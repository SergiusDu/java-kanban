package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.Task;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 * A repository for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored in an
 * internal map.
 */
public final class InMemoryTaskRepository implements TaskRepository {
  public static final String TASK_CAN_T_BE_NULL = "Task can't be null";
  private final NavigableMap<Integer, Task> store = new TreeMap<>();
  private final AtomicInteger index = new AtomicInteger(0);

  /**
   * Adds a new task to the repository. The task must inherit from the {@link Task} class.
   *
   * @param <T> the type of the task being added, which extends {@link Task}
   * @param task the task to be added to the repository
   * @return the added task with its current details
   * @throws NullPointerException if the provided {@code task} is {@code null}
   */
  @Override
  public <T extends Task> T addTask(final T task) {
    Objects.requireNonNull(task, TASK_CAN_T_BE_NULL);
    store.put(task.getId(), task);
    return task;
  }

  /**
   * Updates an existing com.tasktracker.task in the repository with new details. The
   * com.tasktracker.task to update must have a valid ID already existing in the repository.
   *
   * @param updatedTask the com.tasktracker.task containing the updated details, including a valid
   *     ID
   * @return the updated {@link Task} after applying the changes
   * @throws NullPointerException if the provided {@code updatedTask} is {@code null}
   * @throws NoSuchElementException if no com.tasktracker.task exists in the repository for the
   *     specified ID
   */
  public Task updateTask(final Task updatedTask) throws NoSuchElementException {
    Objects.requireNonNull(updatedTask, "Updated com.tasktracker.task can't be null");
    checkTaskExists(updatedTask.getId());
    store.put(updatedTask.getId(), updatedTask);
    return updatedTask;
  }

  /**
   * Retrieves all tasks stored in the repository.
   *
   * @return an unmodifiable {@link Collection} containing all tasks
   */
  public Collection<Task> getAllTasks() {
    return Collections.unmodifiableCollection(store.values());
  }

  /**
   * Retrieves a com.tasktracker.task by its unique identifier.
   *
   * @param id the unique identifier of the com.tasktracker.task in the repository
   * @return an {@link Optional} containing the com.tasktracker.task if it exists, or an empty
   *     {@link Optional} if it does not
   */
  public Optional<Task> getTaskById(final int id) {
    return Optional.ofNullable(store.get(id));
  }

  /**
   * Removes a com.tasktracker.task from the repository by its unique identifier.
   *
   * @param id the unique identifier of the com.tasktracker.task to remove from the repository
   * @return an {@link Optional} containing the removed com.tasktracker.task, or an empty {@link
   *     Optional} if no com.tasktracker.task was found for the given ID
   * @throws IllegalArgumentException if no com.tasktracker.task exists for the specified ID
   */
  public Optional<Task> removeTask(final int id) {
    checkTaskExists(id);
    return Optional.ofNullable(store.remove(id));
  }

  /**
   * Finds tasks that match the given taskPredicate criteria.
   *
   * @param taskPredicate the {@link Predicate} to apply to each com.tasktracker.task for filtering
   * @return a {@link Collection} of tasks that satisfy the given {@link Predicate}; an empty list
   *     if no such tasks exist
   */
  public Collection<Task> findTasksMatching(final Predicate<Task> taskPredicate) {
    return store.values().stream().filter(taskPredicate).toList();
  }

  /**
   * Removes tasks from the repository that satisfy the given taskPredicate condition.
   *
   * @param taskPredicate the {@link Predicate} used to identify tasks to remove
   */
  public boolean removeMatchingTasks(final Predicate<Task> taskPredicate) {
    Objects.requireNonNull(taskPredicate);
    return store.entrySet().removeIf(entry -> taskPredicate.test(entry.getValue()));
  }

  /**
   * Clears all tasks from the repository, permanently deleting all stored data. After this
   * operation is performed, the repository will be empty. This action is irreversible.
   */
  public void clearAllTasks() {
    store.clear();
  }

  /**
   * Verifies that a com.tasktracker.task with the specified ID exists in the repository.
   *
   * @param id the unique identifier of the com.tasktracker.task
   * @throws NoSuchElementException if no com.tasktracker.task with the specified ID exists
   */
  private void checkTaskExists(final int id) throws NoSuchElementException {
    if (!store.containsKey(id)) {
      throw new NoSuchElementException("Task ID" + id + " does not exist.");
    }
  }

  /**
   * Generates a unique integer ID for a new {@link Task}. The ID is incremented atomically and
   * guaranteed to be unique across all tasks in the repository. The first ID generated is 1.
   *
   * @return a unique integer ID for the new {@link Task}
   */
  @Override
  public int generateId() {
    return this.index.incrementAndGet();
  }
}
