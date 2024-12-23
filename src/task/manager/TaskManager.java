package task.manager;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import task.model.EpicTask;
import task.model.Task;
import task.store.TaskRepository;

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

    public Task addTask(final Task task) {
        return store.addTask(task.getId(), task);
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
