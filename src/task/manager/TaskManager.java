package task.manager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import task.model.EpicTask;
import task.model.Task;
import task.store.TaskRepository;

public class TaskManager<T extends Task> {
  private final TaskRepository<T> store;

  public TaskManager(final TaskRepository<T> store) {
        this.store = store;
    }

  public Collection<T> getAllTasks() {
        return store.getAllTasks();
    }

    public void clearAllTasks() {
        store.clearAllTasks();
    }

  public Optional<T> getTaskById(int id) {
        return store.getTaskById(id);
    }

  public T addTask(final T task) {
        return store.addTask(task.getId(), task);
    }

  public T updateTask(final T task) {
        return store.updateTask(task.getId(), task);
    }

  public Collection<T> getEpicSubtasks(EpicTask epicTask) {
        List<Integer> subtasksIds = epicTask.getSubtaskIds();
        return store.findTasksMatching(task -> subtasksIds.contains(task.getId()));
    }

  public Collection<T> getAllTasksByClass(Class<T> targetClass) {
        return store.findTasksMatching(targetClass::isInstance).stream().toList();
    }
}
