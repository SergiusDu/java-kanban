package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.util.*;
import java.util.function.Predicate;

/**
 * A repository for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique UUIDs and stored in an internal
 * map.
 */
public class InMemoryTaskRepository implements TaskRepository {
  public static final String TASK_CAN_T_BE_NULL = "Task can't be null";
  private final NavigableMap<UUID, Task> store = new TreeMap<>();

  /**
   * Adds a new task to the repository. The task must have a unique ID that isn't already present in
   * the repository.
   *
   * @param task the task to add to the repository
   * @throws NullPointerException if the task is null
   * @throws IllegalArgumentException if a task with the same ID already exists in the repository
   */
  @Override
  public void addTask(final Task task) {
    Objects.requireNonNull(task, TASK_CAN_T_BE_NULL);
    if (store.containsKey(task.getId())) {
      throw new IllegalArgumentException(
          String.format("Task with id %s already exists in store", task.getId()));
    }
    store.put(task.getId(), task);
  }

  /**
   * Updates an existing task in the repository with the provided updated task data.
   *
   * @param updatedTask the task containing the updated data, must have an existing ID in the
   *     repository
   * @return the previous version of the task that was updated
   * @throws TaskNotFoundException if no task exists with the ID of the updated task
   * @throws NullPointerException if the updated task is null
   */
  public Task updateTask(Task updatedTask) throws TaskNotFoundException {
    Objects.requireNonNull(updatedTask, "Updated task can't be null");
    final UUID id = updatedTask.getId();
    if (store.get(id) == null) {
      throw new TaskNotFoundException("Task with ID " + id + " not found for update.");
    }
    return store.put(id, updatedTask);
  }

  /**
   * Retrieves all tasks stored in the repository.
   *
   * @return an unmodifiable Collection containing all tasks
   */
  public List<Task> getAllTasks() {
    return List.copyOf(store.values());
  }

  /**
   * Retrieves a task by its unique identifier.
   *
   * @param id the unique identifier of the task in the repository
   * @return an Optional containing the task if it exists, or an empty Optional if it does not
   */
  public Optional<Task> getTaskById(final UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  /**
   * Removes a task from the repository by its unique identifier.
   *
   * @param id the unique identifier of the task to remove from the repository
   * @return an Optional containing the removed task, or an empty Optional if no task was found with
   *     the given ID
   * @throws NullPointerException if id is null
   */
  public Optional<Task> removeTask(final UUID id) {
    return Optional.ofNullable(store.remove(id));
  }

  /**
   * Finds tasks that match the given predicate criteria.
   *
   * @param taskPredicate the predicate to apply to each task for filtering
   * @return a Collection of tasks that satisfy the given predicate; an empty list if no such tasks
   *     exist
   * @throws NullPointerException if the taskPredicate is null
   */
  public Collection<Task> findTasksMatching(final Predicate<Task> taskPredicate) {
    Objects.requireNonNull(taskPredicate);
    return store.values().stream().filter(taskPredicate).toList();
  }

  /**
   * Removes tasks from the repository that satisfy the given predicate condition.
   *
   * @param taskPredicate the predicate used to identify tasks to remove
   * @return true if any tasks were removed, false otherwise
   * @throws NullPointerException if the specified predicate is null
   */
  public boolean removeMatchingTasks(final Predicate<Task> taskPredicate) {
    Objects.requireNonNull(taskPredicate);
    return store.entrySet().removeIf(entry -> taskPredicate.test(entry.getValue()));
  }

  /**
   * Clears all tasks from the repository, permanently deleting all stored data. After this
   * operation is performed, the repository will be empty.
   */
  public void clearAllTasks() {
    store.clear();
  }
}
