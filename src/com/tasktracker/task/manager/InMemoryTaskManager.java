package com.tasktracker.task.manager;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.TaskRepository;
import com.tasktracker.task.validation.Validator;
import com.tasktracker.task.validation.ValidatorFactory;
import com.tasktracker.util.TypeSafeCaster;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Manages tasks of various types such as Regular Tasks, Sub-Tasks, and Epic Tasks. Provides
 * operations for creating, updating, and removing tasks, as well as managing relationships between
 * tasks like Epic Tasks and their Sub-Tasks.
 */
public class InMemoryTaskManager implements TaskManager {
  public static final String THE_CLASS_TYPE_CANNOT_BE_NULL = "The class type cannot be null.";
  private final TaskRepository store;

  /**
   * Constructs a TaskManager with the given TaskRepository for storing and managing tasks.
   *
   * @param store the repository used to store and retrieve tasks
   * @throws NullPointerException if the given repository is null
   */
  public InMemoryTaskManager(final TaskRepository store) {
    this.store = Objects.requireNonNull(store, "TaskRepository cannot be null.");
  }

  /**
   * Retrieves all tasks currently stored in the repository.
   *
   * @return a collection of all tasks
   */
  @Override
  public Collection<Task> getAllTasks() {
    return store.getAllTasks();
  }

  /** Clears all tasks from the repository. */
  @Override
  public void clearAllTasks() {
    store.clearAllTasks();
  }

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
  @Override
  public <T extends Task> boolean removeTasksByType(final Class<T> clazz)
      throws UnsupportedOperationException {
    Objects.requireNonNull(clazz, "Task type cannot be null.");
    if (clazz == RegularTask.class) {
      return store.removeMatchingTasks(RegularTask.class::isInstance);
    } else if (clazz == SubTask.class) {
      return store.findTasksMatching(SubTask.class::isInstance).stream()
          .map(SubTask.class::cast)
          .map(
              subTask -> {
                removeSubTaskFromEpic(subTask.getEpicTaskId(), subTask.getId());
                return store.removeTaskById(subTask.getId());
              })
          .allMatch(Optional::isPresent);
    } else if (clazz == EpicTask.class) {
      return store.findTasksMatching(EpicTask.class::isInstance).stream()
          .map(EpicTask.class::cast)
          .map(
              epicTask -> {
                epicTask.getSubtaskIds().forEach(store::removeTaskById);
                return store.removeTaskById(epicTask.getId());
              })
          .allMatch(Optional::isPresent);
    } else {
      throw new UnsupportedOperationException(
          "Unsupported com.tasktracker.task type: " + clazz.getSimpleName());
    }
  }

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
  @Override
  public Optional<Task> removeTaskById(final int id) throws UnsupportedOperationException {
    Optional<Task> optionalTask = store.getTaskById(id);
    if (optionalTask.isEmpty()) return Optional.empty();
    Task taskToDelete = optionalTask.get();
    switch (taskToDelete) {
      case RegularTask regularTask -> {
        return store.removeTaskById(regularTask.getId());
      }
      case SubTask subTask -> {
        removeSubTaskFromEpic(subTask.getEpicTaskId(), subTask.getId());
        updateEpicTaskStatus(getTaskOrThrowIfInvalid(subTask.getEpicTaskId(), EpicTask.class));
        return store.removeTaskById(subTask.getId());
      }
      case EpicTask epicTask -> {
        epicTask.getSubtaskIds().forEach(store::removeTaskById);
        return store.removeTaskById(epicTask.getId());
      }
      default ->
          throw new UnsupportedOperationException(
              "Unknown com.tasktracker.task type: " + taskToDelete.getClass().getName());
    }
  }

  /**
   * Retrieves a com.tasktracker.task from the repository by its ID.
   *
   * @param id the ID of the com.tasktracker.task to retrieve
   * @return an {@link Optional} containing the com.tasktracker.task if it exists, or an empty
   *     Optional if not
   */
  @Override
  public Optional<Task> getTaskById(int id) {
    return store.getTaskById(id);
  }

  /**
   * Creates and adds a new Regular Task to the repository.
   *
   * @param regularTaskCreationDTO the DTO containing data for the Regular Task
   * @return the created Regular Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data is invalid
   */
  @Override
  public RegularTask addTask(final RegularTaskCreationDTO regularTaskCreationDTO) {
    Objects.requireNonNull(regularTaskCreationDTO, "RegularTaskCreationDTO cannot be null.");
    validateDto(regularTaskCreationDTO, RegularTaskCreationDTO.class);
    LocalDateTime currentTime = LocalDateTime.now();
    return store.addTask(
        new RegularTask(
            store.generateId(),
            regularTaskCreationDTO.title(),
            regularTaskCreationDTO.description(),
            TaskStatus.NEW,
            currentTime,
            currentTime));
  }

  /**
   * Creates and adds a new Epic Task to the repository.
   *
   * @param epicTaskCreationDTO the DTO containing data for the Epic Task
   * @return the created Epic Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data is invalid
   */
  @Override
  public EpicTask addTask(final EpicTaskCreationDTO epicTaskCreationDTO) {
    Objects.requireNonNull(epicTaskCreationDTO, "EpicTaskCreationDTO cannot be null.");
    validateDto(epicTaskCreationDTO, EpicTaskCreationDTO.class);
    LocalDateTime currentTime = LocalDateTime.now();
    return store.addTask(
        new EpicTask(
            store.generateId(),
            epicTaskCreationDTO.title(),
            epicTaskCreationDTO.description(),
            TaskStatus.NEW,
            Set.of(),
            currentTime,
            currentTime));
  }

  /**
   * Creates and adds a new Sub-Task to the repository, and associates it with its parent Epic Task.
   *
   * @param subTaskCreationDTO the DTO containing data for the Sub-Task
   * @return the created Sub-Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data or associated Epic Task is invalid
   */
  @Override
  public SubTask addTask(final SubTaskCreationDTO subTaskCreationDTO) {
    Objects.requireNonNull(subTaskCreationDTO, "SubTaskCreationDTO cannot be null.");
    validateDto(subTaskCreationDTO, SubTaskCreationDTO.class);
    EpicTask epicTask = getTaskOrThrowIfInvalid(subTaskCreationDTO.epicId(), EpicTask.class);
    LocalDateTime currentTime = LocalDateTime.now();
    SubTask subTask =
        store.addTask(
            new SubTask(
                store.generateId(),
                subTaskCreationDTO.title(),
                subTaskCreationDTO.description(),
                TaskStatus.NEW,
                subTaskCreationDTO.epicId(),
                currentTime,
                currentTime));
    Set<Integer> updatedSubtaskIds = new HashSet<>(epicTask.getSubtaskIds());
    updatedSubtaskIds.add(subTask.getId());
    store.updateTask(
        new EpicTask(
            epicTask.getId(),
            epicTask.getTitle(),
            epicTask.getDescription(),
            epicTask.getStatus(),
            updatedSubtaskIds,
            epicTask.getCreationDate(),
            LocalDateTime.now()));
    return subTask;
  }

  /**
   * Updates an existing Regular Task in the repository.
   *
   * @param regularTaskUpdateDTO the DTO containing updated data for the Regular Task
   * @return the updated Regular Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the com.tasktracker.task data is invalid
   */
  @Override
  public RegularTask updateTask(final RegularTaskUpdateDTO regularTaskUpdateDTO) {
    Objects.requireNonNull(regularTaskUpdateDTO, "RegularTaskUpdateDTO cannot be null.");
    validateDto(regularTaskUpdateDTO, RegularTaskUpdateDTO.class);
    RegularTask currentTask = getTaskOrThrowIfInvalid(regularTaskUpdateDTO.id(), RegularTask.class);
    RegularTask updatedTask =
        new RegularTask(
            regularTaskUpdateDTO.id(),
            regularTaskUpdateDTO.title(),
            regularTaskUpdateDTO.description(),
            regularTaskUpdateDTO.status(),
            currentTask.getCreationDate(),
            LocalDateTime.now());
    return (RegularTask) store.updateTask(updatedTask);
  }

  /**
   * Updates an existing Sub-Task in the repository and modifies the association with its parent
   * Epic Task if necessary.
   *
   * @param subTaskUpdateDTO the DTO containing updated data for the Sub-Task
   * @return the updated Sub-Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the com.tasktracker.task data or associated Epic Task is invalid
   */
  @Override
  public SubTask updateTask(final SubTaskUpdateDTO subTaskUpdateDTO) {
    Objects.requireNonNull(subTaskUpdateDTO, "SubTaskUpdateDTO cannot be null.");
    validateDto(subTaskUpdateDTO, SubTaskUpdateDTO.class);
    SubTask currentSubTask = getTaskOrThrowIfInvalid(subTaskUpdateDTO.id(), SubTask.class);
    validateTaskTypeOrThrow(subTaskUpdateDTO.epicId(), EpicTask.class);
    if (currentSubTask.getEpicTaskId() != subTaskUpdateDTO.epicId()) {
      removeSubTaskFromEpic(currentSubTask.getEpicTaskId(), subTaskUpdateDTO.id());
    }
    SubTask updatedSubTask =
        TypeSafeCaster.castSafely(
            store.updateTask(
                new SubTask(
                    subTaskUpdateDTO.id(),
                    subTaskUpdateDTO.title(),
                    subTaskUpdateDTO.description(),
                    subTaskUpdateDTO.status(),
                    subTaskUpdateDTO.epicId(),
                    currentSubTask.getCreationDate(),
                    LocalDateTime.now())),
            SubTask.class);
    EpicTask epicTask = attachSubTaskToEpicTask(updatedSubTask);
    if (updatedSubTask.getStatus() != epicTask.getStatus()) updateEpicTaskStatus(epicTask);
    return updatedSubTask;
  }

  /**
   * Updates an existing Epic Task in the repository, preserving its Sub-Task associations and
   * status.
   *
   * @param epicTaskUpdateDTO the DTO containing updated data for the Epic Task
   * @return the updated Epic Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the com.tasktracker.task data is invalid
   */
  @Override
  public EpicTask updateTask(final EpicTaskUpdateDTO epicTaskUpdateDTO) {
    Objects.requireNonNull(epicTaskUpdateDTO, "EpicTaskUpdateDTO cannot be null.");
    validateDto(epicTaskUpdateDTO, EpicTaskUpdateDTO.class);
    EpicTask currentTask = getTaskOrThrowIfInvalid(epicTaskUpdateDTO.id(), EpicTask.class);
    EpicTask updatedTask =
        new EpicTask(
            epicTaskUpdateDTO.id(),
            epicTaskUpdateDTO.title(),
            epicTaskUpdateDTO.description(),
            currentTask.getStatus(),
            currentTask.getSubtaskIds(),
            currentTask.getCreationDate(),
            LocalDateTime.now());
    return (EpicTask) store.updateTask(updatedTask);
  }

  /**
   * Retrieves all Sub-Tasks associated with the given Epic Task.
   *
   * @param epicId the ID of the Epic Task
   * @return a collection of associated Sub-Tasks
   * @throws ValidationException if the Epic Task does not exist or is invalid
   */
  @Override
  public Collection<SubTask> getEpicSubtasks(int epicId) {
    Set<Integer> subtaskIds = getTaskOrThrowIfInvalid(epicId, EpicTask.class).getSubtaskIds();
    return subtaskIds.stream()
        .map(subtaskId -> getTaskOrThrowIfInvalid(subtaskId, SubTask.class))
        .toList();
  }

  /**
   * Retrieves all tasks of the specified class type stored in the repository.
   *
   * @param targetClass the class type of tasks to retrieve
   * @return a collection of tasks matching the specified class type
   * @throws NullPointerException if the specified class type is null
   */
  @Override
  public <T extends Task> Collection<T> getAllTasksByClass(Class<T> targetClass) {
    Objects.requireNonNull(targetClass, THE_CLASS_TYPE_CANNOT_BE_NULL);
    return store.findTasksMatching(targetClass::isInstance).stream()
        .map(targetClass::cast)
        .toList();
  }

  /**
   * Retrieves a com.tasktracker.task by its ID and ensures it matches the specified class type.
   *
   * @param taskId the ID of the com.tasktracker.task to retrieve
   * @param clazz the class type the com.tasktracker.task must match
   * @param <T> the desired com.tasktracker.task type
   * @return the com.tasktracker.task cast to the specified type
   * @throws NullPointerException if the class type is null
   * @throws ValidationException if the com.tasktracker.task does not exist or does not match the
   *     specified type
   */
  private <T> T getTaskOrThrowIfInvalid(final int taskId, final Class<T> clazz)
      throws ValidationException {
    Objects.requireNonNull(clazz, THE_CLASS_TYPE_CANNOT_BE_NULL);
    return store
        .getTaskById(taskId)
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .orElseThrow(
            () ->
                new ValidationException(
                    "Task with ID " + taskId + " is not an instance of " + clazz.getSimpleName()));
  }

  /**
   * Validates that a com.tasktracker.task exists and matches the specified class type.
   *
   * @param taskId the ID of the com.tasktracker.task to validate
   * @param clazz the class type the com.tasktracker.task must match
   * @param <T> the desired com.tasktracker.task type
   * @throws NullPointerException if the class type is null
   * @throws ValidationException if the com.tasktracker.task does not exist or does not match the
   *     specified type
   */
  private <T> void validateTaskTypeOrThrow(final int taskId, final Class<T> clazz)
      throws ValidationException {
    Objects.requireNonNull(clazz, THE_CLASS_TYPE_CANNOT_BE_NULL);
    boolean isValid = store.getTaskById(taskId).filter(clazz::isInstance).isPresent();
    if (!isValid) {
      throw new ValidationException(
          "Error: Task with ID "
              + taskId
              + " does not exist or is not of the required type "
              + clazz.getSimpleName()
              + ".");
    }
  }

  /**
   * Calculates the status of an Epic Task based on the statuses of its associated Sub-Tasks.
   *
   * @param subtaskIds the set of Sub-Task IDs associated with the Epic Task
   * @return the calculated status of the Epic Task
   * @throws NullPointerException if the Sub-Task IDs set is null
   * @throws ValidationException if a Sub-Task associated with an ID does not exist
   */
  private TaskStatus calculateEpicTaskStatus(final Set<Integer> subtaskIds) {
    Objects.requireNonNull(subtaskIds, "Subtask IDs cannot be null.");
    Set<TaskStatus> subTaskStatuses =
        subtaskIds.stream()
            .map(
                subtaskId ->
                    store
                        .getTaskById(subtaskId)
                        .orElseThrow(
                            () ->
                                new ValidationException(
                                    "Subtask with ID " + subtaskId + " does not exist"))
                        .getStatus())
            .collect(Collectors.toSet());
    if (subTaskStatuses.isEmpty()) return TaskStatus.NEW;
    AtomicBoolean areAllDone = new AtomicBoolean(true);
    AtomicBoolean areAllNew = new AtomicBoolean(true);
    AtomicBoolean stopProcessing = new AtomicBoolean(false);
    subTaskStatuses.parallelStream()
        .takeWhile(status -> !stopProcessing.get())
        .forEach(
            status -> {
              if (areAllDone.get() && status != TaskStatus.DONE) areAllDone.set(false);
              if (areAllNew.get() && status != TaskStatus.NEW) areAllNew.set(false);
              if (!areAllNew.get() && !areAllDone.get()) stopProcessing.set(true);
            });
    if (areAllNew.get()) return TaskStatus.NEW;
    if (areAllDone.get()) return TaskStatus.DONE;
    return TaskStatus.IN_PROGRESS;
  }

  /**
   * Updates the status of an Epic Task by recalculating it based on its associated Sub-Tasks.
   *
   * @param epicTask the Epic Task to update
   * @throws NullPointerException if the Epic Task is null
   */
  private void updateEpicTaskStatus(final EpicTask epicTask) {
    Objects.requireNonNull(epicTask, "Epic Task can't be null.");
    store.updateTask(
        new EpicTask(
            epicTask.getId(),
            epicTask.getTitle(),
            epicTask.getDescription(),
            calculateEpicTaskStatus(epicTask.getSubtaskIds()),
            epicTask.getSubtaskIds(),
            epicTask.getCreationDate(),
            LocalDateTime.now()));
  }

  /**
   * Validates a given DTO using the appropriate validator for its class type.
   *
   * @param dto the DTO object to validate
   * @param clazz the class type of the DTO
   * @param <T> the type of the DTO
   * @throws NullPointerException if the DTO or its class type is null
   * @throws ValidationException if the DTO is invalid
   */
  private <T> void validateDto(final T dto, final Class<T> clazz) throws ValidationException {
    Objects.requireNonNull(dto, "The DTO cannot be null.");
    Objects.requireNonNull(clazz, THE_CLASS_TYPE_CANNOT_BE_NULL);
    Validator<T> validator = ValidatorFactory.getValidator(clazz);
    validator.validate(dto);
  }

  /**
   * Associates a Sub-Task with its parent Epic Task if not already associated and updates the Epic
   * Task.
   *
   * @param subTask the Sub-Task to associate with the Epic Task
   * @return the updated Epic Task
   * @throws ValidationException if the Sub-Task or associated Epic Task is invalid
   */
  private EpicTask attachSubTaskToEpicTask(final SubTask subTask) {
    EpicTask epicTask =
        TypeSafeCaster.castSafelyOrThrow(
            store.getTaskById(subTask.getEpicTaskId()), EpicTask.class);
    Set<Integer> epicSubTaskIds = new HashSet<>(epicTask.getSubtaskIds());
    if (!epicTask.getSubtaskIds().contains(subTask.getId())) {
      epicSubTaskIds.add(subTask.getId());
      store.updateTask(
          new EpicTask(
              epicTask.getId(),
              epicTask.getTitle(),
              epicTask.getDescription(),
              epicTask.getStatus(),
              epicSubTaskIds,
              epicTask.getCreationDate(),
              LocalDateTime.now()));
    }
    return epicTask;
  }

  /**
   * Removes a Sub-Task from its associated Epic Task and updates the Epic Task in the repository.
   *
   * @param epicId the ID of the Epic Task
   * @param subtaskId the ID of the Sub-Task to remove
   * @throws ValidationException if the Epic Task or Sub-Task does not exist or is invalid
   */
  private void removeSubTaskFromEpic(int epicId, int subtaskId) {
    EpicTask currentEpicTask =
        TypeSafeCaster.castSafelyOrThrow(store.getTaskById(epicId), EpicTask.class);
    Set<Integer> previousEpicTaskSubTaskIds = new HashSet<>(currentEpicTask.getSubtaskIds());
    previousEpicTaskSubTaskIds.remove(subtaskId);
    store.updateTask(
        new EpicTask(
            currentEpicTask.getId(),
            currentEpicTask.getTitle(),
            currentEpicTask.getDescription(),
            currentEpicTask.getStatus(),
            previousEpicTaskSubTaskIds,
            currentEpicTask.getCreationDate(),
            LocalDateTime.now()));
  }
}
