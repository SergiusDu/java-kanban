package com.tasktracker.task.manager;

import com.tasktracker.task.dto.*;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.service.EpicTaskAggregatedResult;
import com.tasktracker.task.service.EpicTaskStatusAndTimeCollector;
import com.tasktracker.task.service.ScheduleIndex;
import com.tasktracker.task.service.TreeSetScheduleIndex;
import com.tasktracker.task.store.TaskRepository;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import com.tasktracker.task.validation.Validator;
import com.tasktracker.task.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages tasks of various types such as Regular Tasks, Sub-Tasks, and Epic Tasks. Provides
 * operations for creating, updating, and removing tasks, as well as managing relationships between
 * tasks like Epic Tasks and their Sub-Tasks.
 */
public class TaskManagerImpl implements TaskManager {
  public static final String THE_CLASS_TYPE_CANNOT_BE_NULL = "The class type cannot be null.";
  private final TaskRepository store;
  private final HistoryManager historyManager;
  private final ScheduleIndex index;

  /**
   * Constructs a TaskManager with the given {@link TaskRepository} for storing and managing tasks.
   *
   * @param store the repository used to store and retrieve tasks
   */
  public TaskManagerImpl(final TaskRepository store, final HistoryManager historyManager) {
    this.store = Objects.requireNonNull(store, "TaskRepository cannot be null.");
    this.historyManager = Objects.requireNonNull(historyManager, "History Manager can't be null");
    this.index = new TreeSetScheduleIndex();
  }

  @Override
  public List<Task> getPrioritizedTasks() {
    return index.asOrderedList();
  }

  /**
   * Generates a unique UUID that is not currently in use for any task in the store.
   *
   * @return a new unique UUID that does not exist in the task store
   */
  private UUID generateId() {
    UUID id;
    do {
      id = UUID.randomUUID();
    } while (store.getTaskById(id).isPresent());
    return id;
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
    store.getAllTasks().forEach(this::removeTaskFromStoreAndHistory);
  }

  @Override
  public <T extends Task> void removeTasksByType(final Class<T> clazz)
      throws UnsupportedOperationException {
    Objects.requireNonNull(clazz, "Task type cannot be null.");
    if (clazz.equals(RegularTask.class)) {
      store
          .findTasksMatching(RegularTask.class::isInstance)
          .forEach(this::removeTaskFromStoreAndHistory);
    } else if (clazz.equals(SubTask.class)) {
      store.findTasksMatching(SubTask.class::isInstance).stream()
          .map(SubTask.class::cast)
          .collect(
              Collectors.groupingBy(
                  SubTask::getEpicTaskId, Collectors.mapping(SubTask::getId, Collectors.toSet())))
          .forEach(
              (epicId, removedIds) -> {
                removedIds.forEach(this::removeTaskFromStoreAndHistory);
                try {
                  EpicTask epicTask = getMatchingTaskOrThrow(epicId, EpicTask.class);
                  store.updateTask(
                      new EpicTask(
                          epicTask.getId(),
                          epicTask.getTitle(),
                          epicTask.getDescription(),
                          TaskStatus.NEW,
                          Collections.emptySet(),
                          epicTask.getCreationDate(),
                          LocalDateTime.now(),
                          null,
                          null));
                } catch (ValidationException e) {
                  throw new IllegalArgumentException(
                      "Invalid task state while updating epic task: " + e.getMessage(), e);
                } catch (TaskNotFoundException e) {
                  throw new NoSuchElementException(
                      "Task not found while updating epic task: " + e.getMessage(), e);
                }
              });
    } else if (clazz.equals(EpicTask.class)) {
      store.findTasksMatching(EpicTask.class::isInstance).stream()
          .map(EpicTask.class::cast)
          .forEach(
              epicTask -> {
                Set<UUID> subtaskIds = new HashSet<>(epicTask.getSubtaskIds());
                subtaskIds.forEach(this::removeTaskFromStoreAndHistory);
                removeTaskFromStoreAndHistory(epicTask);
              });
    } else {
      throw new UnsupportedOperationException(
          "Unsupported com.tasktracker.task type: " + clazz.getSimpleName());
    }
  }

  @Override
  public Optional<Task> removeTaskById(final UUID id)
      throws UnsupportedOperationException, ValidationException, TaskNotFoundException {
    Optional<Task> optionalTask = store.getTaskById(id);
    if (optionalTask.isEmpty()) return Optional.empty();
    Task taskToDelete = optionalTask.get();
    switch (taskToDelete) {
      case RegularTask regularTask -> {
        return removeTaskFromStoreAndHistory(regularTask);
      }
      case SubTask subTask -> {
        Optional<Task> removedTask = removeTaskFromStoreAndHistory(subTask);
        removeSubTaskIdFromEpicTask(subTask.getEpicTaskId(), subTask.getId());
        EpicTask parentEpicTask = getMatchingTaskOrThrow(subTask.getEpicTaskId(), EpicTask.class);
        Set<UUID> remainingSubTaskIds =
            store.findTasksMatching(SubTask.class::isInstance).stream()
                .map(SubTask.class::cast)
                .filter(st -> st.getEpicTaskId().equals(subTask.getEpicTaskId()))
                .map(SubTask::getId)
                .collect(Collectors.toSet());
        EpicTaskAggregatedResult aggregatedProperties =
            aggregateEpicSubTaskProperties(remainingSubTaskIds);
        store.updateTask(
            new EpicTask(
                parentEpicTask.getId(),
                parentEpicTask.getTitle(),
                parentEpicTask.getDescription(),
                aggregatedProperties.status(),
                remainingSubTaskIds,
                parentEpicTask.getCreationDate(),
                LocalDateTime.now(),
                aggregatedProperties.startTime(),
                aggregatedProperties.duration()));
        return removedTask;
      }
      case EpicTask epicTask -> {
        epicTask.getSubtaskIds().forEach(this::removeTaskFromStoreAndHistory);
        return removeTaskFromStoreAndHistory(epicTask);
      }
      default ->
          throw new UnsupportedOperationException(
              "Unknown com.tasktracker.task type: " + taskToDelete.getClass().getName());
    }
  }

  private Optional<Task> removeTaskFromStoreAndHistory(UUID id) {
    Optional<Task> task = store.getTaskById(id);
    task.ifPresent(index::remove);
    historyManager.remove(id);
    return store.removeTask(id);
  }

  private Optional<Task> removeTaskFromStoreAndHistory(Task task) {
    index.remove(task);
    historyManager.remove(task.getId());
    return store.removeTask(task.getId());
  }

  @Override
  public Optional<Task> getTask(UUID id) {
    Optional<Task> result = store.getTaskById(id);
    result.ifPresent(historyManager::put);
    return result;
  }

  /**
   * Creates and adds a new regular task to the repository based on the provided DTO.
   *
   * @param dto The DTO containing the required fields for creating a regular task
   * @return The newly created and persisted RegularTask
   * @throws ValidationException if the DTO validation fails
   * @throws NullPointerException if the DTO is null
   */
  @Override
  public RegularTask addTask(final RegularTaskCreationDTO dto) throws ValidationException {
    validateDto(dto, RegularTaskCreationDTO.class);
    LocalDateTime creationTimestamp = LocalDateTime.now();
    RegularTask newTask =
        new RegularTask(
            generateId(),
            dto.title(),
            dto.description(),
            TaskStatus.NEW,
            creationTimestamp,
            creationTimestamp,
            dto.startTime(),
            dto.duration());
    index.add(newTask);
    return store.addTask(newTask);
  }

  /**
   * Validates that if a task exists with the given ID, it matches the expected type. Does nothing
   * if no task exists with the given ID, allowing for creation of new tasks.
   *
   * @param <T> the expected task type that extends Task
   * @param taskId the ID of the task to validate
   * @param taskClass the class representing the expected task type
   * @throws ValidationException if a task exists but does not match the expected type
   * @throws NullPointerException if taskId or taskClass is null
   */
  private <T extends Task> void validateExistingTaskClassType(UUID taskId, Class<T> taskClass)
      throws ValidationException {
    Optional<Task> existingTask = store.getTaskById(taskId);
    if (existingTask.isPresent() && (!taskClass.isInstance(existingTask.get()))) {
      throw new ValidationException(
          String.format(
              "Task with ID %s exists but is not a %s", taskId, taskClass.getSimpleName()));
    }
  }

  /**
   * Creates and adds a new Epic Task to the repository based on the provided DTO. The Epic Task is
   * initialized with NEW status, empty subtasks set and current timestamps.
   *
   * @param dto The DTO containing creation data including title, description and optional start
   *     time
   * @return The newly created and persisted Epic Task
   * @throws ValidationException if the DTO validation fails
   * @throws NullPointerException if the DTO is null
   */
  @Override
  public EpicTask addTask(final EpicTaskCreationDTO dto) throws ValidationException {
    validateDto(dto, EpicTaskCreationDTO.class);
    LocalDateTime currentTime = LocalDateTime.now();
    EpicTask newTask =
        new EpicTask(
            generateId(),
            dto.title(),
            dto.description(),
            TaskStatus.NEW,
            Set.of(),
            currentTime,
            currentTime,
            dto.startTime(),
            null);
    return store.addTask(newTask);
  }

  /**
   * Creates and adds a new Sub-Task to the repository, associating it with an existing Epic Task.
   * The new Sub-Task is initialized with NEW status and current timestamps.
   *
   * @param dto the DTO containing Sub-Task creation data including title, description, Epic Task
   *     ID, start time, and duration
   * @return the newly created and persisted Sub-Task
   * @throws ValidationException if the DTO validation fails or the Epic Task does not exist
   * @throws TaskNotFoundException if the referenced Epic Task cannot be found
   * @throws NullPointerException if the DTO is null
   */
  @Override
  public SubTask addTask(final SubTaskCreationDTO dto)
      throws ValidationException, TaskNotFoundException {
    Objects.requireNonNull(dto, "SubTaskCreationDTO cannot be null.");
    validateDto(dto, SubTaskCreationDTO.class);
    validateExistingTaskClassType(dto.epicId(), EpicTask.class);
    LocalDateTime currentTime = LocalDateTime.now();
    SubTask subTask =
        new SubTask(
            generateId(),
            dto.title(),
            dto.description(),
            TaskStatus.NEW,
            dto.epicId(),
            currentTime,
            currentTime,
            dto.startTime(),
            dto.duration());
    index.add(subTask);
    SubTask addedTask = store.addTask(subTask);
    attachSubTaskToEpicTask(subTask);
    return addedTask;
  }

  @Override
  public RegularTask updateTask(final RegularTaskUpdateDTO dto)
      throws ValidationException, TaskNotFoundException {
    Objects.requireNonNull(dto, "RegularTaskUpdateDTO cannot be null.");
    validateDto(dto, RegularTaskUpdateDTO.class);
    RegularTask currentTask = getMatchingTaskOrThrow(dto.id(), RegularTask.class);
    RegularTask updatedTask =
        new RegularTask(
            dto.id(),
            dto.title(),
            dto.description(),
            dto.status(),
            currentTask.getCreationDate(),
            LocalDateTime.now(),
            dto.startTime(),
            dto.duration());
    index.update(currentTask, updatedTask);
    return (RegularTask) store.updateTask(updatedTask);
  }

  @Override
  public SubTask updateTask(final SubTaskUpdateDTO dto)
      throws ValidationException, TaskNotFoundException {
    Objects.requireNonNull(dto, "SubTaskUpdateDTO cannot be null.");
    validateDto(dto, SubTaskUpdateDTO.class);
    SubTask oldSubTask = getMatchingTaskOrThrow(dto.id(), SubTask.class);
    SubTask newSubTask =
        new SubTask(
            dto.id(),
            dto.title(),
            dto.description(),
            dto.status(),
            dto.epicId(),
            oldSubTask.getCreationDate(),
            LocalDateTime.now(),
            dto.startTime(),
            dto.duration());
    var previousSubTask = store.updateTask(newSubTask);
    EpicTask oldEpicTask = getMatchingTaskOrThrow(oldSubTask.getEpicTaskId(), EpicTask.class);
    Set<UUID> subtaskIds = new HashSet<>(oldEpicTask.getSubtaskIds());
    subtaskIds.add(dto.id());
    EpicTaskAggregatedResult aggregatedEpicData = aggregateEpicSubTaskProperties(subtaskIds);
    EpicTask newEpicTask =
        new EpicTask(
            oldEpicTask.getId(),
            oldEpicTask.getTitle(),
            oldEpicTask.getDescription(),
            aggregatedEpicData.status(),
            subtaskIds,
            oldEpicTask.getCreationDate(),
            oldEpicTask.getUpdateDate(),
            aggregatedEpicData.startTime(),
            aggregatedEpicData.duration());
    index.updateEpicAndSubtask(oldSubTask, newSubTask, oldEpicTask, newEpicTask);
    store.updateTask(newEpicTask);
    return (SubTask) previousSubTask;
  }

  /**
   * Updates an existing Epic Task in the repository with a new title and description. The status is
   * calculated based on the statuses of its subtasks, and the start time and duration are
   * calculated based on its associated subtasks.
   *
   * @param dto the DTO containing updated data for the Epic Task
   * @return the updated Epic Task
   * @throws ValidationException if the Epic Task data is invalid or the task does not exist
   * @throws TaskNotFoundException if no task exists with the ID in the DTO
   * @throws NullPointerException if the DTO parameter is null
   */
  @Override
  public EpicTask updateTask(final EpicTaskUpdateDTO dto)
      throws ValidationException, TaskNotFoundException {
    Objects.requireNonNull(dto, "EpicTaskUpdateDTO cannot be null.");
    validateDto(dto, EpicTaskUpdateDTO.class);
    EpicTask oldTask = getMatchingTaskOrThrow(dto.id(), EpicTask.class);
    EpicTask newTask =
        new EpicTask(
            dto.id(),
            dto.title(),
            dto.description(),
            oldTask.getStatus(),
            oldTask.getSubtaskIds(),
            oldTask.getCreationDate(),
            LocalDateTime.now(),
            oldTask.getStartTime(),
            oldTask.getDuration());
    index.update(oldTask, newTask);
    return (EpicTask) store.updateTask(newTask);
  }

  /**
   * Retrieves all Sub-Tasks associated with the given Epic Task.
   *
   * @param epicId the ID of the Epic Task
   * @return a collection of associated Sub-Tasks
   * @throws ValidationException if the Epic Task does not exist or is invalid
   */
  @Override
  public Collection<SubTask> getEpicSubtasks(UUID epicId) throws ValidationException {
    EpicTask correspondentEpicTask =
        store
            .getTaskById(epicId)
            .filter(EpicTask.class::isInstance)
            .map(EpicTask.class::cast)
            .orElseThrow(
                () -> new ValidationException("Task with ID " + epicId + " is not an Epic Task"));
    return correspondentEpicTask.getSubtaskIds().stream()
        .map(store::getTaskById)
        .flatMap(Optional::stream)
        .map(SubTask.class::cast)
        .toList();
  }

  /**
   * Retrieves all tasks of the specified class type stored in the repository.
   *
   * @param targetClass the class type of tasks to retrieve
   * @return a collection of tasks matching the specified class type
   */
  @Override
  public <T extends Task> Collection<T> getAllTasksByClass(Class<T> targetClass) {
    Objects.requireNonNull(targetClass, THE_CLASS_TYPE_CANNOT_BE_NULL);
    return store.findTasksMatching(targetClass::isInstance).stream()
        .map(targetClass::cast)
        .toList();
  }

  /**
   * Retrieves the complete history of tasks as a collection of {@link Task} objects. Only tasks
   * that are still present in the {@link TaskRepository} are included in the result.
   *
   * @return a collection of {@link Task} objects present in the history and the repository
   */
  @Override
  public Collection<Task> getHistory() {
    return historyManager.getHistory().stream()
        .map(taskView -> store.getTaskById(taskView.getTaskId()))
        .flatMap(Optional::stream)
        .toList();
  }

  private EpicTaskAggregatedResult aggregateEpicSubTaskProperties(final Set<UUID> subTasksIds) {
    Objects.requireNonNull(subTasksIds, "SubTasks IDs can't be null.");
    return subTasksIds.stream()
        .map(
            id -> {
              try {
                return getMatchingTaskOrThrow(id, SubTask.class);
              } catch (ValidationException e) {
                throw new IllegalStateException(
                    "Failed to obtain SubTask for Epic task aggregate properties calculation", e);
              }
            })
        .filter(Objects::nonNull)
        .collect(EpicTaskStatusAndTimeCollector.aggregateEpicSubTaskPropertiesCollector());
  }

  /**
   * Validates a given DTO using the appropriate validator for its class type.
   *
   * @param dto the DTO object to validate
   * @param clazz the class type of the DTO
   * @param <T> the type of the DTO
   * @throws ValidationException if the DTO is invalid
   */
  private <T> void validateDto(final T dto, final Class<T> clazz) throws ValidationException {
    Objects.requireNonNull(dto, "The DTO cannot be null.");
    Objects.requireNonNull(clazz, THE_CLASS_TYPE_CANNOT_BE_NULL);
    Validator<T> validator = ValidatorFactory.getValidator(clazz);
    validator.validate(dto);
  }

  /**
   * Attaches a SubTask to its associated EpicTask in the repository. If the SubTask is already
   * attached to the EpicTask, no changes are made. When attaching a new SubTask, the EpicTask's
   * status, start time, and duration are recalculated based on all SubTasks.
   *
   * @param subTask the SubTask to attach to its EpicTask
   * @throws ValidationException if the EpicTask does not exist or validation fails
   * @throws TaskNotFoundException if the referenced EpicTask cannot be found
   * @throws NullPointerException if subTask is null
   */
  private void attachSubTaskToEpicTask(final SubTask subTask)
      throws ValidationException, TaskNotFoundException {
    EpicTask oldEpicTask = getMatchingTaskOrThrow(subTask.getEpicTaskId(), EpicTask.class);
    Set<UUID> subTaskIds = new HashSet<>(oldEpicTask.getSubtaskIds());
    if (oldEpicTask.getSubtaskIds().contains(subTask.getId())) return;
    subTaskIds.add(subTask.getId());
    EpicTaskAggregatedResult epicsAggregatedStatusAndTimeProperties =
        aggregateEpicSubTaskProperties(subTaskIds);
    EpicTask newEpicTask =
        new EpicTask(
            oldEpicTask.getId(),
            oldEpicTask.getTitle(),
            oldEpicTask.getDescription(),
            epicsAggregatedStatusAndTimeProperties.status(),
            subTaskIds,
            oldEpicTask.getCreationDate(),
            LocalDateTime.now(),
            epicsAggregatedStatusAndTimeProperties.startTime(),
            epicsAggregatedStatusAndTimeProperties.duration());
    index.update(oldEpicTask, newEpicTask);
    store.updateTask(newEpicTask);
  }

  private void removeSubTaskIdFromEpicTask(UUID epicId, UUID subtaskId)
      throws TaskNotFoundException, ValidationException {
    EpicTask oldEpicTask = getMatchingTaskOrThrow(epicId, EpicTask.class);
    Set<UUID> updatedSubTaskIds = new HashSet<>(oldEpicTask.getSubtaskIds());
    updatedSubTaskIds.remove(subtaskId);
    EpicTask newEpicTask =
        new EpicTask(
            oldEpicTask.getId(),
            oldEpicTask.getTitle(),
            oldEpicTask.getDescription(),
            oldEpicTask.getStatus(),
            updatedSubTaskIds,
            oldEpicTask.getCreationDate(),
            LocalDateTime.now(),
            oldEpicTask.getStartTime(),
            oldEpicTask.getDuration());
    index.update(oldEpicTask, newEpicTask);
    store.updateTask(newEpicTask);
  }

  /**
   * Retrieves a task by ID and validates that it matches the expected type.
   *
   * @param <T> The expected task type that extends Task
   * @param taskId The ID of the task to retrieve
   * @param clazz The class representing the expected task type
   * @return The task cast to the expected type
   * @throws NullPointerException if taskId or clazz is null
   * @throws ValidationException if a task does not exist or does not match the expected type
   */
  private <T extends Task> T getMatchingTaskOrThrow(UUID taskId, Class<T> clazz)
      throws ValidationException {
    Objects.requireNonNull(taskId, "TaskId can't be null");
    Task task = getTaskByIdOrThrowIfNotExist(taskId);
    validateTaskTypeMatch(task, clazz);
    return clazz.cast(task);
  }

  /**
   * Retrieves a task by its ID from the store or throws an exception if it doesn't exist.
   *
   * @param taskId The unique identifier of the task to retrieve
   * @return The task if found
   * @throws NullPointerException if taskId is null
   * @throws ValidationException if no task exists with the given ID
   */
  private Task getTaskByIdOrThrowIfNotExist(UUID taskId) throws ValidationException {
    Objects.requireNonNull(taskId, "TaskId can't be null");
    return store
        .getTaskById(taskId)
        .orElseThrow(
            () -> new ValidationException(String.format("Task with ID %s does not exist", taskId)));
  }

  /**
   * Validates if a given task matches the expected type.
   *
   * @param task The task instance to validate
   * @param expectedType The expected class type that the task should match
   * @param <T> Type parameter bounded by Task class
   * @throws ValidationException if the task is not of the expected type
   * @throws NullPointerException if a task or expectedType is null
   */
  private <T extends Task> void validateTaskTypeMatch(Task task, Class<T> expectedType)
      throws ValidationException {
    Objects.requireNonNull(task, "Task ID cannot be null");
    Objects.requireNonNull(expectedType, "Expected task type cannot be null");

    if (!expectedType.isAssignableFrom(task.getClass())) {
      throw new ValidationException(
          String.format(
              "Task with ID %s has type %s but expected type %s",
              task.getId(), task.getClass().getSimpleName(), expectedType.getSimpleName()));
    }
  }
}
