package com.tasktracker.task.manager;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskManager {
  /**
   * Retrieves all tasks currently stored in the repository.
   *
   * @return a collection of all tasks
   */
  Collection<Task> getAllTasks();

  /** Clears all tasks from the repository. */
  void clearAllTasks();

  List<Task> getPrioritizedTasks();

  <T extends Task> void removeTasksByType(Class<T> clazz) throws UnsupportedOperationException;

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
  Optional<Task> removeTaskById(UUID id)
      throws UnsupportedOperationException, ValidationException, TaskNotFoundException;

  /**
   * Retrieves a com.tasktracker.task from the repository by its ID.
   *
   * @param id the ID of the com.tasktracker.task to retrieve
   * @return an {@link Optional} containing the com.tasktracker.task if it exists, or an empty
   *     Optional if not
   */
  Optional<Task> getTask(UUID id);

  void addTask(RegularTaskCreationDTO regularTaskCreationDTO) throws ValidationException;

  void addTask(EpicTaskCreationDTO epicTaskCreationDTO) throws ValidationException;

  void addTask(SubTaskCreationDTO subTaskCreationDTO)
      throws ValidationException, TaskNotFoundException;

  RegularTask updateTask(RegularTaskUpdateDTO regularTaskUpdateDTO)
      throws ValidationException, TaskNotFoundException;

  SubTask updateTask(SubTaskUpdateDTO subTaskUpdateDTO)
      throws ValidationException, TaskNotFoundException;

  EpicTask updateTask(EpicTaskUpdateDTO epicTaskUpdateDTO)
      throws ValidationException, TaskNotFoundException;

  Collection<SubTask> getEpicSubtasks(UUID epicId) throws ValidationException;

  <T extends Task> Collection<T> getAllTasksByClass(Class<T> targetClass);

  Collection<Task> getHistory();
}
