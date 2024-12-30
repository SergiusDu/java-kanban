package com.tasktracker.task.store;

import com.tasktracker.task.dto.EpicTaskCreationDTO;
import com.tasktracker.task.dto.RegularTaskCreationDTO;
import com.tasktracker.task.dto.SubTaskCreationDTO;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Interface for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored in an
 * underlying storage mechanism.
 */
public interface TaskRepository {

  /**
   * Adds a new regular com.tasktracker.task to the repository based on the provided {@link
   * RegularTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the regular com.tasktracker.task to
   *     be created
   * @return the created {@link RegularTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  RegularTask addTask(RegularTaskCreationDTO dto);

  /**
   * Adds a new epic com.tasktracker.task to the repository based on the provided {@link
   * EpicTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the epic com.tasktracker.task to be
   *     created
   * @return the created {@link EpicTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  EpicTask addTask(EpicTaskCreationDTO dto);

  /**
   * Adds a new sub-com.tasktracker.task to the repository based on the provided {@link
   * SubTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the sub-com.tasktracker.task to be
   *     created
   * @return the created {@link SubTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  SubTask addTask(SubTaskCreationDTO dto);

  /**
   * Updates an existing com.tasktracker.task in the repository. Replaces the com.tasktracker.task
   * with the new com.tasktracker.task details based on its unique ID. The com.tasktracker.task must
   * have a valid ID already existing in the repository.
   *
   * @param updatedTask the new com.tasktracker.task details, including an existing valid ID
   * @return the updated {@link Task} after replacing the previous com.tasktracker.task
   * @throws NullPointerException if the provided com.tasktracker.task is {@code null}
   * @throws IllegalArgumentException if no com.tasktracker.task exists with the specified ID
   */
  Task updateTask(Task updatedTask) throws NoSuchElementException;

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
   * @throws IllegalArgumentException if no com.tasktracker.task exists for the specified ID
   */
  Optional<Task> removeTaskById(int id);

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
   * @throws NullPointerException if {@code taskPredicate} is {@code null}
   */
  boolean removeMatchingTasks(Predicate<Task> taskPredicate);

  /**
   * Clears all tasks from the repository, permanently deleting all stored data. After this
   * operation is performed, the repository will be empty. This action is irreversible.
   */
  void clearAllTasks();
}
