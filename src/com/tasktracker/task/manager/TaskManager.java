package com.tasktracker.task.manager;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.util.Collection;
import java.util.Optional;

public interface TaskManager {
  /**
   * Retrieves all tasks currently stored in the repository.
   *
   * @return a collection of all tasks
   */
  Collection<Task> getAllTasks();

  /** Clears all tasks from the repository. */
  void clearAllTasks();

  /**
   * Removes tasks from the repository that match the specified com.tasktracker.task type. If the
   * com.tasktracker.task type is {@link EpicTask}, all its associated Sub-Tasks will also be
   * removed.
   *
   * @param clazz the class type of the tasks to remove
   * @param <T> the generic type extending {@link Task} representing the com.tasktracker.task type
   * @return {@code true} if at least one com.tasktracker.task was removed, {@code false} otherwise
   * @throws NullPointerException if the com.tasktracker.task type is {@code null}
   * @throws UnsupportedOperationException if the provided com.tasktracker.task type is unsupported
   */
  <T extends Task> boolean removeTasksByType(Class<T> clazz) throws UnsupportedOperationException;

  /**
   * Removes a com.tasktracker.task from the repository by its ID. If the com.tasktracker.task is a
   * Regular Task, it is directly removed. If the com.tasktracker.task is a Sub-Task, it is removed
   * and its parent Epic Task's status is updated accordingly. If the com.tasktracker.task is an
   * Epic Task, its associated Sub-Tasks are also removed along with it.
   *
   * @param id the ID of the com.tasktracker.task to remove
   * @return an {@link Optional} containing the removed com.tasktracker.task if it existed, or an
   *     empty Optional if not
   * @throws UnsupportedOperationException if the com.tasktracker.task type is unknown or
   *     unsupported
   */
  Optional<Task> removeTaskById(int id) throws UnsupportedOperationException;

  /**
   * Retrieves a com.tasktracker.task from the repository by its ID.
   *
   * @param id the ID of the com.tasktracker.task to retrieve
   * @return an {@link Optional} containing the com.tasktracker.task if it exists, or an empty
   *     Optional if not
   */
  Optional<Task> getTaskById(int id);

  /**
   * Creates and adds a new Regular Task to the repository.
   *
   * @param regularTaskCreationDTO the DTO containing data for the Regular Task
   * @return the created Regular Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data is invalid
   */
  RegularTask addTask(RegularTaskCreationDTO regularTaskCreationDTO);

  /**
   * Creates and adds a new Epic Task to the repository.
   *
   * @param epicTaskCreationDTO the DTO containing data for the Epic Task
   * @return the created Epic Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data is invalid
   */
  EpicTask addTask(EpicTaskCreationDTO epicTaskCreationDTO);

  /**
   * Creates and adds a new Sub-Task to the repository, and associates it with its parent Epic Task.
   *
   * @param subTaskCreationDTO the DTO containing data for the Sub-Task
   * @return the created Sub-Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data or associated Epic Task is invalid
   */
  SubTask addTask(SubTaskCreationDTO subTaskCreationDTO);

  /**
   * Updates an existing Regular Task in the repository.
   *
   * @param regularTaskUpdateDTO the DTO containing updated data for the Regular Task
   * @return the updated Regular Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the com.tasktracker.task data is invalid
   */
  RegularTask updateTask(RegularTaskUpdateDTO regularTaskUpdateDTO);

  /**
   * Updates an existing Sub-Task in the repository and modifies the association with its parent
   * Epic Task if necessary.
   *
   * @param subTaskUpdateDTO the DTO containing updated data for the Sub-Task
   * @return the updated Sub-Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the com.tasktracker.task data or associated Epic Task is invalid
   */
  SubTask updateTask(SubTaskUpdateDTO subTaskUpdateDTO);

  /**
   * Updates an existing Epic Task in the repository, preserving its Sub-Task associations and
   * status.
   *
   * @param epicTaskUpdateDTO the DTO containing updated data for the Epic Task
   * @return the updated Epic Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the com.tasktracker.task data is invalid
   */
  EpicTask updateTask(EpicTaskUpdateDTO epicTaskUpdateDTO);

  /**
   * Retrieves all Sub-Tasks associated with the given Epic Task.
   *
   * @param epicId the ID of the Epic Task
   * @return a collection of associated Sub-Tasks
   * @throws ValidationException if the Epic Task does not exist or is invalid
   */
  Collection<SubTask> getEpicSubtasks(int epicId);

  /**
   * Retrieves all tasks of the specified class type stored in the repository.
   *
   * @param targetClass the class type of tasks to retrieve
   * @return a collection of tasks matching the specified class type
   * @throws NullPointerException if the specified class type is null
   */
  <T extends Task> Collection<T> getAllTasksByClass(Class<T> targetClass);
}
