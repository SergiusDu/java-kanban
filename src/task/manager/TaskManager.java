package task.manager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import task.dto.RegularTaskCreationDto;
import task.model.*;
import task.store.TaskRepository;
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

  public RegularTask addTask(final RegularTaskCreationDto taskCreationDto) {
    RegularTask newTask =
        new RegularTask(
            0,
            taskCreationDto.title(),
            taskCreationDto.description(),
            RegularTaskCreationDto.status);
    return TypeSafeCaster.castSafely(store.addTask(newTask), RegularTask.class);
  }

  public EpicTask addTask(final EpicTask task) {
    return TypeSafeCaster.castSafely(store.addTask(task), EpicTask.class);
  }

  public SubTask addTask(final SubTask task) {
    return TypeSafeCaster.castSafely(store.addTask(task), SubTask.class);
  }

  public Task updateTask(final Task task) {
    return store.updateTask(task.getId(), task);
  }

  public Collection<Task> getEpicSubtasks(EpicTask epicTask) {
    List<Integer> subtasksIds = epicTask.getSubtaskIds();
    return store.findTasksMatching(task -> subtasksIds.contains(task.getId()));
  }

  public Collection<Task> getAllTasksByClass(Class<Task> targetClass) {
    return store.findTasksMatching(targetClass::isInstance).stream().toList();
  }
}
