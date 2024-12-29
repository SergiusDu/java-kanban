package task.store.interfaces;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import task.dto.EpicTaskCreationDTO;
import task.dto.RegularTaskCreationDTO;
import task.dto.SubTaskCreationDTO;
import task.model.implementations.EpicTask;
import task.model.implementations.RegularTask;
import task.model.implementations.SubTask;
import task.model.implementations.Task;

/**
 * Interface for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored in an
 * underlying storage mechanism.
 */
public interface TaskRepository {

  /**
   * Adds a new regular task to the repository based on the provided {@link RegularTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the regular task to be created
   * @return the created {@link RegularTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  RegularTask addTask(RegularTaskCreationDTO dto);

  /**
   * Adds a new epic task to the repository based on the provided {@link EpicTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the epic task to be created
   * @return the created {@link EpicTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  EpicTask addTask(EpicTaskCreationDTO dto);

  /**
   * Adds a new sub-task to the repository based on the provided {@link SubTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the sub-task to be created
   * @return the created {@link SubTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  SubTask addTask(SubTaskCreationDTO dto);

  /**
   * Updates an existing task in the repository. Replaces the task with the new task details based
   * on its unique ID. The task must have a valid ID already existing in the repository.
   *
   * @param updatedTask the new task details, including an existing valid ID
   * @return the updated {@link Task} after replacing the previous task
   * @throws NullPointerException if the provided task is {@code null}
   * @throws IllegalArgumentException if no task exists with the specified ID
   */
  Task updateTask(Task updatedTask);

  /**
   * Retrieves all tasks stored in the repository.
   *
   * @return an unmodifiable {@link Collection} containing all tasks
   */
  Collection<Task> getAllTasks();

  /**
   * Retrieves a task by its unique identifier.
   *
   * @param id the unique identifier of the task in the repository
   * @return an {@link Optional} containing the task if it exists, or an empty {@link Optional} if
   *     it does not
   */
  Optional<Task> getTaskById(int id);

  /**
   * Removes a task from the repository by its unique identifier.
   *
   * @param id the unique identifier of the task to remove from the repository
   * @return an {@link Optional} containing the removed task, or an empty {@link Optional} if no
   *     task was found for the given ID
   * @throws IllegalArgumentException if no task exists for the specified ID
   */
  Optional<Task> removeTaskById(int id);

  /**
   * Finds tasks that match the given {@link Predicate} criteria.
   *
   * @param taskPredicate the {@link Predicate} to apply to each task for filtering
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
