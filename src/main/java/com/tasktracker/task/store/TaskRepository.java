package com.tasktracker.task.store;

import com.tasktracker.task.model.implementations.Task;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Interface for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored in an
 * underlying storage mechanism.
 */
public interface TaskRepository {

  /**
   * Adds a new task of type {@link Task} or its subclasses to the repository.
   *
   * @param <T> the type of {@link Task} being added
   * @param task the task to be added to the repository
   * @return the added task with an auto-generated unique ID
   */
  <T extends Task> T addTask(T task);

  /**
   * Updates an existing {@link Task} in the repository with new details. The {@link Task} to update
   * must have a valid ID that already exists in the repository.
   *
   * @param updatedTask the {@link Task} containing the updated details, including a valid ID
   * @return an {@link Optional} containing the previous {@link Task} details, or an empty {@link
   *     Optional} if no task with the given ID exists
   */
  Task updateTask(Task updatedTask);

  /**
   * Retrieves all tasks stored in the repository.
   *
   * @return an unmodifiable {@link Collection} containing all tasks
   */
  Collection<Task> getAllTasks();

  /**
   * Retrieves a com.tasktracker.task by its unique identifier.
   *
   * @param id the unique identifier of the com.tasktracker.task in the repository
   * @return an {@link Optional} containing the com.tasktracker.task if it exists, or an empty
   *     {@link Optional} if it does not
   */
  Optional<Task> getTaskById(int id);

  /**
   * Removes a com.tasktracker.task from the repository by its unique identifier.
   *
   * @param id the unique identifier of the com.tasktracker.task to remove from the repository
   * @return an {@link Optional} containing the removed com.tasktracker.task, or an empty {@link
   *     Optional} if no com.tasktracker.task was found for the given ID
   */
  Optional<Task> removeTask(int id);

  /**
   * Finds tasks that match the given {@link Predicate} criteria.
   *
   * @param taskPredicate the {@link Predicate} to apply to each com.tasktracker.task for filtering
   * @return a {@link Collection} of tasks that satisfy the given {@link Predicate}; an empty list
   *     if no such tasks exist
   */
  Collection<Task> findTasksMatching(Predicate<Task> taskPredicate);

  /**
   * Removes tasks from the repository that satisfy the given {@link Predicate} condition.
   *
   * @param taskPredicate the {@link Predicate} used to identify tasks to remove
   * @return {@code true} if any tasks were removed, {@code false} otherwise
   */
  boolean removeMatchingTasks(Predicate<Task> taskPredicate);

  /**
   * Clears all tasks from the repository, permanently deleting all stored data. After this
   * operation is performed, the repository will be empty. This action is irreversible.
   */
  void clearAllTasks();

  /**
   * Generates a unique integer ID for new {@link Task} objects in the repository. The ID is
   * generated as one greater than the highest current ID in the repository. If no tasks exist, the
   * ID generation starts with 0.
   *
   * @return a unique integer ID for a new {@link Task}
   */
  int generateId();
}
