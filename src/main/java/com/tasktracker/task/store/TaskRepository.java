package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Interface for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique UUIDs and stored in an underlying
 * storage mechanism.
 */
public interface TaskRepository {

  /**
   * Adds a new task to the repository and returns it. The task must have a unique ID that isn't
   * already present in the repository.
   *
   * @param task the task to add to the repository
   * @return the added task
   * @throws NullPointerException if the task is null
   * @throws IllegalArgumentException if a task with the same ID already exists in the repository
   */
  <T extends Task> T addTask(final T task);

  /**
   * Updates an existing task in the repository with the provided updated task data.
   *
   * @param updatedTask the task containing the updated data, must have an existing ID in the
   *     repository
   * @return the previous version of the task that was updated
   * @throws TaskNotFoundException if no task exists with the ID of the updated task
   * @throws NullPointerException if the updated task is null
   */
  Task updateTask(Task updatedTask) throws TaskNotFoundException;

  /**
   * Retrieves all tasks stored in the repository.
   *
   * @return an unmodifiable Collection containing all tasks
   */
  Collection<Task> getAllTasks();

  /**
   * Retrieves a task by its UUID.
   *
   * @param id the UUID of the task
   * @return an Optional containing the task if found, or empty if not found
   */
  Optional<Task> getTaskById(UUID id);

  /**
   * Removes a task from the repository.
   *
   * @param id the UUID of the task to remove
   * @return an Optional containing the removed task, or empty if not found
   */
  Optional<Task> removeTask(UUID id);

  /**
   * Finds tasks that match the given predicate.
   *
   * @param taskPredicate the predicate to filter tasks
   * @return a Collection of matching tasks, or empty collection if none found
   */
  Collection<Task> findTasksMatching(Predicate<Task> taskPredicate);

  /**
   * Removes tasks matching the given predicate.
   *
   * @param taskPredicate the predicate to identify tasks to remove
   * @return true if any tasks were removed, false otherwise
   */
  boolean removeMatchingTasks(Predicate<Task> taskPredicate);

  /** Removes all tasks from the repository. */
  void clearAllTasks();
}
