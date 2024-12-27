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

public class TaskManager {
  public static final String THE_CLASS_TYPE_CANNOT_BE_NULL = "The class type cannot be null.";
  private final TaskRepository store;

  public TaskManager(final TaskRepository store) {
    this.store = Objects.requireNonNull(store, "TaskRepository cannot be null.");
  }

  public Collection<Task> getAllTasks() {
    return store.getAllTasks();
  }

  public void clearAllTasks() {
    store.clearAllTasks();
  }

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

  public Optional<Task> getTaskById(int id) {
    return store.getTaskById(id);
  }

  public RegularTask addTask(final RegularTaskCreationDTO regularTaskCreationDTO) {
    Objects.requireNonNull(regularTaskCreationDTO, "RegularTaskCreationDTO cannot be null.");
    validateDto(regularTaskCreationDTO, RegularTaskCreationDTO.class);
    return store.addTask(regularTaskCreationDTO);
  }

  public EpicTask addTask(final EpicTaskCreationDTO epicTaskCreationDTO) {
    Objects.requireNonNull(epicTaskCreationDTO, "EpicTaskCreationDTO cannot be null.");
    validateDto(epicTaskCreationDTO, EpicTaskCreationDTO.class);
    return store.addTask(epicTaskCreationDTO);
  }

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

  public Collection<SubTask> getEpicSubtasks(int epicId) {
    Set<Integer> subtaskIds = getTaskOrThrowIfInvalid(epicId, EpicTask.class).getSubtaskIds();
    return subtaskIds.stream()
        .map(subtaskId -> getTaskOrThrowIfInvalid(subtaskId, SubTask.class))
        .toList();
  }

  public Collection<Task> getAllTasksByClass(Class<Task> targetClass) {
    Objects.requireNonNull(targetClass, THE_CLASS_TYPE_CANNOT_BE_NULL);
    return store.findTasksMatching(targetClass::isInstance).stream().toList();
  }

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

  private <T> void validateDto(final T dto, final Class<T> clazz) throws ValidationException {
    Objects.requireNonNull(dto, "The DTO cannot be null.");
    Objects.requireNonNull(clazz, THE_CLASS_TYPE_CANNOT_BE_NULL);
    Validator<T> validator = ValidatorFactory.getValidator(clazz);
    validator.validate(dto);
  }

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
