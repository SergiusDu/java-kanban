package task.manager;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import task.dto.*;
import task.exception.ValidationException;
import task.model.enums.TaskStatus;
import task.model.implementations.EpicTask;
import task.model.implementations.RegularTask;
import task.model.implementations.SubTask;
import task.model.implementations.Task;
import task.store.TaskRepository;
import task.validation.Validator;
import task.validation.ValidatorFactory;
import util.TypeSafeCaster;

/**
 * Manages tasks of various types such as Regular Tasks, Sub-Tasks, and Epic Tasks. Provides
 * operations for creating, updating, and removing tasks, as well as managing relationships between
 * tasks like Epic Tasks and their Sub-Tasks.
 */
public class TaskManager {
  public static final String THE_CLASS_TYPE_CANNOT_BE_NULL = "The class type cannot be null.";
  private final TaskRepository store;

  /**
   * Constructs a TaskManager with the given TaskRepository for storing and managing tasks.
   *
   * @param store the repository used to store and retrieve tasks
   * @throws NullPointerException if the given repository is null
   */
  public TaskManager(final TaskRepository store) {
    this.store = Objects.requireNonNull(store, "TaskRepository cannot be null.");
  }

  /**
   * Retrieves all tasks currently stored in the repository.
   *
   * @return a collection of all tasks
   */
  public Collection<Task> getAllTasks() {
    return store.getAllTasks();
  }

  /** Clears all tasks from the repository. */
  public void clearAllTasks() {
    store.clearAllTasks();
  }

  /**
   * Removes all tasks of the specified type from the repository.
   *
   * @param clazz the class type of tasks to be removed (e.g., RegularTask, SubTask, EpicTask)
   * @param <T> the task type
   * @return true if at least one task was removed, false otherwise
   * @throws NullPointerException if the specified task class is null
   * @throws ValidationException if the provided task type is unsupported
   */
  public <T extends Task> boolean removeTasksByType(final Class<T> clazz) {
    Objects.requireNonNull(clazz, "Task type cannot be null.");
    if (clazz == RegularTask.class) {
      return store.removeMatchingTasks(RegularTask.class::isInstance);
    } else if (clazz == SubTask.class) {
      return store.removeMatchingTasks(SubTask.class::isInstance);
    } else if (clazz == EpicTask.class) {
      return store.removeMatchingTasks(EpicTask.class::isInstance);
    } else {
      throw new ValidationException("Unsupported task type: " + clazz.getSimpleName());
    }
  }

  /**
   * Removes a task from the repository by its ID. If the task is a Sub-Task, its association with
   * its parent Epic Task is also updated.
   *
   * @param id the ID of the task to remove
   * @return an {@link Optional} containing the removed task if it existed, or an empty Optional if
   *     not
   * @throws ValidationException if no task with the specified ID exists
   */
  public Optional<Task> removeTaskById(final int id) {
    Task taskToDelete =
        store
            .getTaskById(id)
            .orElseThrow(
                () -> new ValidationException("Error: Task with ID " + id + " does not exist."));
    if (taskToDelete instanceof SubTask subTask) {
      removeSubTaskFromEpic(subTask.getEpicTaskId(), subTask.getId());
      updateEpicTaskStatus(getTaskOrThrowIfInvalid(subTask.getEpicTaskId(), EpicTask.class));
    }
    return store.removeTaskById(id);
  }

  /**
   * Retrieves a task from the repository by its ID.
   *
   * @param id the ID of the task to retrieve
   * @return an {@link Optional} containing the task if it exists, or an empty Optional if not
   */
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
  public RegularTask addTask(final RegularTaskCreationDTO regularTaskCreationDTO) {
    Objects.requireNonNull(regularTaskCreationDTO, "RegularTaskCreationDTO cannot be null.");
    validateDto(regularTaskCreationDTO, RegularTaskCreationDTO.class);
    return store.addTask(regularTaskCreationDTO);
  }

  /**
   * Creates and adds a new Epic Task to the repository.
   *
   * @param epicTaskCreationDTO the DTO containing data for the Epic Task
   * @return the created Epic Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data is invalid
   */
  public EpicTask addTask(final EpicTaskCreationDTO epicTaskCreationDTO) {
    Objects.requireNonNull(epicTaskCreationDTO, "EpicTaskCreationDTO cannot be null.");
    validateDto(epicTaskCreationDTO, EpicTaskCreationDTO.class);
    return store.addTask(epicTaskCreationDTO);
  }

  /**
   * Creates and adds a new Sub-Task to the repository, and associates it with its parent Epic Task.
   *
   * @param subTaskCreationDTO the DTO containing data for the Sub-Task
   * @return the created Sub-Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the DTO data or associated Epic Task is invalid
   */
  public SubTask addTask(final SubTaskCreationDTO subTaskCreationDTO) {
    Objects.requireNonNull(subTaskCreationDTO, "SubTaskCreationDTO cannot be null.");
    validateDto(subTaskCreationDTO, SubTaskCreationDTO.class);
    EpicTask epicTask = getTaskOrThrowIfInvalid(subTaskCreationDTO.epicId(), EpicTask.class);
    SubTask subTask = store.addTask(subTaskCreationDTO);
    Set<Integer> updatedSubtaskIds = new HashSet<>(epicTask.getSubtaskIds());
    updatedSubtaskIds.add(subTask.getId());
    store.updateTask(
        new EpicTask(
            epicTask.getId(),
            epicTask.getTitle(),
            epicTask.getDescription(),
            epicTask.getStatus(),
            updatedSubtaskIds));
    return subTask;
  }

  /**
   * Updates an existing Regular Task in the repository.
   *
   * @param regularTaskUpdateDTO the DTO containing updated data for the Regular Task
   * @return the updated Regular Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the task data is invalid
   */
  public RegularTask updateTask(final RegularTaskUpdateDTO regularTaskUpdateDTO) {
    Objects.requireNonNull(regularTaskUpdateDTO, "RegularTaskUpdateDTO cannot be null.");
    validateDto(regularTaskUpdateDTO, RegularTaskUpdateDTO.class);
    validateTaskTypeOrThrow(regularTaskUpdateDTO.id(), RegularTask.class);
    RegularTask updatedTask =
        new RegularTask(
            regularTaskUpdateDTO.id(),
            regularTaskUpdateDTO.title(),
            regularTaskUpdateDTO.description(),
            regularTaskUpdateDTO.status());
    return (RegularTask) store.updateTask(updatedTask);
  }

  /**
   * Updates an existing Sub-Task in the repository and modifies the association with its parent
   * Epic Task if necessary.
   *
   * @param subTaskUpdateDTO the DTO containing updated data for the Sub-Task
   * @return the updated Sub-Task
   * @throws NullPointerException if the DTO is null
   * @throws ValidationException if the task data or associated Epic Task is invalid
   */
  public SubTask updateTask(final SubTaskUpdateDTO subTaskUpdateDTO) {
    Objects.requireNonNull(subTaskUpdateDTO, "SubTaskUpdateDTO cannot be null.");
    validateDto(subTaskUpdateDTO, SubTaskUpdateDTO.class);
    validateTaskTypeOrThrow(subTaskUpdateDTO.id(), SubTask.class);
    validateTaskTypeOrThrow(subTaskUpdateDTO.epicId(), EpicTask.class);
    SubTask currentSubTask =
        TypeSafeCaster.castSafelyOrThrow(store.getTaskById(subTaskUpdateDTO.id()), SubTask.class);
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
                    subTaskUpdateDTO.epicId())),
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
   * @throws ValidationException if the task data is invalid
   */
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
            currentTask.getSubtaskIds());
    return (EpicTask) store.updateTask(updatedTask);
  }

  /**
   * Retrieves all Sub-Tasks associated with the given Epic Task.
   *
   * @param epicId the ID of the Epic Task
   * @return a collection of associated Sub-Tasks
   * @throws ValidationException if the Epic Task does not exist or is invalid
   */
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
  public Collection<Task> getAllTasksByClass(Class<Task> targetClass) {
    Objects.requireNonNull(targetClass, THE_CLASS_TYPE_CANNOT_BE_NULL);
    return store.findTasksMatching(targetClass::isInstance).stream().toList();
  }

  /**
   * Retrieves a task by its ID and ensures it matches the specified class type.
   *
   * @param taskId the ID of the task to retrieve
   * @param clazz the class type the task must match
   * @param <T> the desired task type
   * @return the task cast to the specified type
   * @throws NullPointerException if the class type is null
   * @throws ValidationException if the task does not exist or does not match the specified type
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
   * Validates that a task exists and matches the specified class type.
   *
   * @param taskId the ID of the task to validate
   * @param clazz the class type the task must match
   * @param <T> the desired task type
   * @throws NullPointerException if the class type is null
   * @throws ValidationException if the task does not exist or does not match the specified type
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
    EpicTask refreshedEpicTask =
        new EpicTask(
            epicTask.getId(),
            epicTask.getTitle(),
            epicTask.getDescription(),
            calculateEpicTaskStatus(epicTask.getSubtaskIds()),
            epicTask.getSubtaskIds());
    store.updateTask(refreshedEpicTask);
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
              epicSubTaskIds));
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
            previousEpicTaskSubTaskIds));
  }
}
