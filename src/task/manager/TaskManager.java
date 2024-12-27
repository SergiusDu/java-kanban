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
  private final TaskRepository store;

  public TaskManager(final TaskRepository store) {
    this.store = store;
  }

  public Collection<Task> getAllTasks() {
    return store.getAllTasks();
  }

  public void clearAllTasks() {
    store.clearAllTasks();
  }

  public Optional<Task> getTaskById(int id) {
    return store.getTaskById(id);
  }

  public RegularTask addTask(final RegularTaskCreationDTO regularTaskCreationDTO) {
    validateDto(regularTaskCreationDTO, RegularTaskCreationDTO.class);
    return store.addTask(regularTaskCreationDTO);
  }

  public EpicTask addTask(final EpicTaskCreationDTO epicTaskCreationDTO) {
    validateDto(epicTaskCreationDTO, EpicTaskCreationDTO.class);
    return store.addTask(epicTaskCreationDTO);
  }

  public SubTask addTask(final SubTaskCreationDTO subTaskCreationDTO) {
    validateDto(subTaskCreationDTO, SubTaskCreationDTO.class);
    EpicTask epicTask =
        validateTypeAndGetTaskFromStore(subTaskCreationDTO.epicId(), EpicTask.class);
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
    validateDto(regularTaskUpdateDTO, RegularTaskUpdateDTO.class);
    validateTaskTypeInStore(regularTaskUpdateDTO.id(), RegularTask.class);
    RegularTask updatedTask =
        new RegularTask(
            regularTaskUpdateDTO.id(),
            regularTaskUpdateDTO.title(),
            regularTaskUpdateDTO.description(),
            regularTaskUpdateDTO.status());
    return (RegularTask) store.updateTask(updatedTask);
  }

  public SubTask updateTask(final SubTaskUpdateDTO subTaskUpdateDTO) {
    validateDto(subTaskUpdateDTO, SubTaskUpdateDTO.class);
    validateTaskTypeInStore(subTaskUpdateDTO.id(), SubTask.class);
    validateTaskTypeInStore(subTaskUpdateDTO.epicId(), EpicTask.class);

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
    if (updatedSubTask.getStatus() != epicTask.getStatus()) refreshEpicTaskStatus(epicTask);
    return updatedSubTask;
  }

  public EpicTask updateTask(final EpicTaskUpdateDTO epicTaskUpdateDTO) {
    validateDto(epicTaskUpdateDTO, EpicTaskUpdateDTO.class);
    EpicTask currentTask = validateTypeAndGetTaskFromStore(epicTaskUpdateDTO.id(), EpicTask.class);
    EpicTask updatedTask =
        new EpicTask(
            epicTaskUpdateDTO.id(),
            epicTaskUpdateDTO.title(),
            epicTaskUpdateDTO.description(),
            currentTask.getStatus(),
            currentTask.getSubtaskIds());
    return (EpicTask) store.updateTask(updatedTask);
  }

  public Collection<Task> getEpicSubtasks(EpicTask epicTask) {
    Set<Integer> subtasksIds = epicTask.getSubtaskIds();
    return store.findTasksMatching(task -> subtasksIds.contains(task.getId()));
  }

  public Collection<Task> getAllTasksByClass(Class<Task> targetClass) {
    return store.findTasksMatching(targetClass::isInstance).stream().toList();
  }

  private <T> T validateTypeAndGetTaskFromStore(int taskId, Class<T> clazz) {
    return store
        .getTaskById(taskId)
        .filter(clazz::isInstance)
        .map(clazz::cast)
        .orElseThrow(
            () ->
                new ValidationException(
                    "Task with ID " + taskId + " is not an instance of " + clazz.getSimpleName()));
  }

  private <T> void validateTaskTypeInStore(int taskId, Class<T> clazz) {
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
    AtomicBoolean areAllDone = new AtomicBoolean(true);
    AtomicBoolean areAllNew = new AtomicBoolean(true);
    subTaskStatuses.parallelStream()
        .forEach(
            status -> {
              if (areAllDone.get() && status != TaskStatus.DONE) areAllDone.set(false);
              if (areAllNew.get() && status != TaskStatus.NEW) areAllNew.set(false);
              if (!areAllNew.get() && !areAllDone.get()) {}
            });
    if (areAllNew.get()) return TaskStatus.NEW;
    if (areAllDone.get()) return TaskStatus.DONE;
    return TaskStatus.IN_PROGRESS;
  }

  private void refreshEpicTaskStatus(final EpicTask epicTask) {
    EpicTask refreshedEpicTask =
        new EpicTask(
            epicTask.getId(),
            epicTask.getTitle(),
            epicTask.getDescription(),
            calculateEpicTaskStatus(epicTask.getSubtaskIds()),
            epicTask.getSubtaskIds());
    store.updateTask(refreshedEpicTask);
  }

  private <T> void validateDto(T dto, Class<T> clazz) throws ValidationException {
    Validator<T> validator = ValidatorFactory.getValidator(clazz);
    validator.validate(dto);
  }

  private EpicTask attachSubTaskToEpicTask(SubTask subTask) {
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
