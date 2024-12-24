package task.store;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import task.model.Task;

/**
 * A repository for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored
 * in an internal map.
 */
public class TaskRepository {
    private final Map<Integer, Task> taskStore = new HashMap<>();

  /**
   * Adds a new task to the repository. The task is identified by its unique ID, and if a task with
   * the same ID already exists in the repository, the operation will fail.
   *
   * @param task the task to be added to the repository
   * @return the {@link Task} object added to the repository
   * @throws NullPointerException if the provided task is {@code null}
   * @throws IllegalArgumentException if a task with the same ID already exists in the repository
   */
  public Task addTask(final Task task) {
        Objects.requireNonNull(task, "Task can't be null");
    int taskId = task.getId();
    if (taskStore.containsKey(taskId)) {
      throw new IllegalArgumentException("Task with the same ID already exists: " + taskId);
    }
    taskStore.put(taskId, task);
    return task;
    }

    /**
     * Updates an existing task in the repository. Replaces the task with the specified ID
     * with the new task data.
     *
     * @param id          the unique identifier of the task to update
     * @param updatedTask the new task details
     * @return the updated {@link Task} after replacing the previous task with the specified ID
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
     * @return a {@link Stream} of {@link Map.Entry} objects, each containing a task ID as the key and a {@link Task} as the value
     */
    public Stream<Map.Entry<Integer, Task>> getAllTasksStream() {
        return taskStore.entrySet().stream();
    }

    /**
     * Removes a task from the repository by its unique identifier.
     *
     * @param id the unique identifier of the task to remove
     * @return an {@link Optional} containing the removed task, or an empty {@link Optional} if no task was found for the given ID
     * @throws IllegalArgumentException if no task exists for the specified ID
     */
    public Optional<Task> removeTaskById(final int id) {
        checkTaskExists(id);
        return Optional.ofNullable(taskStore.remove(id));
    }

    /**
     * Finds tasks that match the given taskPredicate criteria.
     *
     * @param taskPredicate the {@link Predicate} to apply to each task
     * @return a {@link Collection} of tasks that satisfy the given {@link Predicate}; an empty list if no such tasks exist
     */
    public Collection<Task> findTasksMatching(final Predicate<Task> taskPredicate) {
        return taskStore.values().stream().filter(taskPredicate).toList();
    }

    /**
     * Removes tasks from the repository that satisfy the given taskPredicate condition.
     *
     * @param taskPredicate the {@link Predicate} used to identify tasks to remove
     */
    public void removeMatchingTasks (final Predicate<Task> taskPredicate) {
        taskStore.entrySet().removeIf(entry -> taskPredicate.test(entry.getValue()));
    }

    /**
     * Clears all tasks from the repository, making it empty. This operation is irreversible and all stored tasks will be lost.
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
