package task.store;

import task.model.Task;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A repository for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored
 * in an internal map.
 */
public class TaskRepository {
    private final Map<Integer, Task> taskStore = new HashMap<>();
    
    /**
     * Adds a new task to the repository. If a task with the same ID already exists,
     * it is replaced with the specified task.
     *
     * @param id    the unique identifier for the task
     * @param task  the task to be added or replaced
     * @return the previous task associated with the ID, or {@code null} if none existed
     * @throws NullPointerException if the provided task is {@code null}
     */
    public Task addTask(final int id, final Task task) {
        Objects.requireNonNull(task, "Task can't be null");
        return taskStore.put(id, task);
    }

    /**
     * Updates an existing task in the repository. Replaces the task with the specified ID
     * with the new task data.
     *
     * @param id          the unique identifier of the task to update
     * @param updatedTask the new task details
     * @return the updated task
     * @throws NullPointerException if the provided task is {@code null}
     * @throws IllegalArgumentException if no task exists for the specified ID
     */
    public Task updateTask(final int id, final Task updatedTask) {
        Objects.requireNonNull(updatedTask, "Updated task can't be null");
        checkTaskExists(id);
        taskStore.put(id, updatedTask);
        return updatedTask;
    }

    /**
     * Retrieves all tasks stored in the repository.
     *
     * @return an unmodifiable {@link Collection} containing all tasks
     */
    public Collection<Task> getAllTasks() {
        return Collections.unmodifiableCollection(taskStore.values());
    }

    /**
     * Retrieves a task by its unique identifier.
     *
     * @param id the unique identifier of the task
     * @return an {@link Optional} containing the task if it exists, or an empty {@link Optional} if it does not
     */
    public Optional<Task> getTaskById(final int id) {
        return Optional.ofNullable(taskStore.get(id));
    }

    /**
     * Returns a stream of all tasks stored in the repository.
     *
     * @return a {@link Stream} of key-value pairs where the key is the task ID and the value is the {@link Task}
     */
    public Stream<Map.Entry<Integer, Task>> getAllTasksStream() {
        return taskStore.entrySet().stream();
    }

    /**
     * Removes a task from the repository by its unique identifier.
     *
     * @param id the unique identifier of the task to remove
     * @return the removed task
     * @throws IllegalArgumentException if no task exists for the specified ID
     */
    public Task removeTaskById(final int id) {
        checkTaskExists(id);
        return taskStore.remove(id);
    }

    /**
     * Finds tasks that match the given filter criteria.
     *
     * @param filter the {@link Predicate} to apply to each task
     * @return a {@link Collection} of tasks that match the filter
     */
    public Collection<Task> findTasksMatching(final Predicate<Task> filter) {
        return taskStore.values().stream().filter(filter).toList();
    }

    /**
     * Removes tasks from the repository that satisfy the given filter condition.
     *
     * @param filter the {@link Predicate} used to identify tasks to remove
     */
    public void removeMatchingTasks (final Predicate<Task> filter) {
        taskStore.entrySet().removeIf(entry -> filter.test(entry.getValue()));
    }

    /**
     * Clears all tasks from the repository, making it empty.
     */
    public void clearAllTasks() {
        taskStore.clear();
    }

    /**
     * Checks if a task with the specified ID exists in the repository.
     *
     * @param id the unique identifier of the task
     * @return {@code true} if a task with the ID exists, {@code false} otherwise
     */
    public boolean containsTask(final int id) {
        return taskStore.containsKey(id);
    }

    /**
     * Verifies that a task with the specified ID exists in the repository.
     *
     * @param id the unique identifier of the task
     * @throws IllegalArgumentException if no task with the specified ID exists
     */
    private void checkTaskExists(final int id) {
        if(!taskStore.containsKey(id)) {
            throw new IllegalArgumentException("Task ID" + id  + "does not exist.");
        }
    }
}
