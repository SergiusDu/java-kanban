package task.manager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import task.model.EpicTask;
import task.model.RegularTask;
import task.model.SubTask;
import task.model.Task;
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

  public RegularTask addTask(final RegularTask task) {
    return TypeSafeCaster.castSafely(store.addTask(task), RegularTask.class);
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
