package task.store;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import task.model.Task;

/**
 * A repository for managing {@link Task}-derived objects, providing operations for adding,
 * updating, retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored
 * in an internal map. The repository works with objects of type {@code <T extends Task>}.
 */
public class TaskRepository<T extends Task> {
  private final Map<Integer, T> taskStore = new HashMap<>();

  /**
   * Adds a new task to the repository. If a task with the same ID already exists, it is replaced
   * with the specified task.
   *
   * @param id the unique identifier for the task
   * @param task the task to be added or replaced
   * @return the previous task associated with the ID, or {@code null} if none existed
   * @throws NullPointerException if the provided task is {@code null}
   */
  public T addTask(final int id, final T task) {
        Objects.requireNonNull(task, "Task can't be null");
        return taskStore.put(id, task);
    }

  /**
   * Updates an existing task in the repository. Replaces the task with the specified ID with the
   * new task data.
   *
   * @param id the unique identifier of the task to update
   * @param updatedTask the new task details
   * @return the updated task
   * @throws NullPointerException if the provided task is {@code null}
   * @throws IllegalArgumentException if no task exists for the specified ID
   */
  public T updateTask(final int id, final T updatedTask) {
        Objects.requireNonNull(updatedTask, "Updated task can't be null");
        checkTaskExists(id);
        taskStore.put(id, updatedTask);
        return updatedTask;
    }

  /**
   * Retrieves all tasks stored in the repository.
   *
   * @return an unmodifiable {@link Collection} containing all objects of type {@code <T>}
   */
  public Collection<T> getAllTasks() {
        return Collections.unmodifiableCollection(taskStore.values());
    }

  /**
   * Retrieves a task by its unique identifier.
   *
   * @param id the unique identifier of the task
   * @return an {@link Optional} containing the task if it exists, or an empty {@link Optional} if
   *     it does not
   */
  public Optional<T> getTaskById(final int id) {
        return Optional.ofNullable(taskStore.get(id));
    }

  /**
   * Returns a stream of all tasks stored in the repository.
   *
   * @return a {@link Stream} of key-value pairs where the key is the task ID and the value is the
   *     {@code <T>}
   */
  public Stream<Map.Entry<Integer, T>> getAllTasksStream() {
        return taskStore.entrySet().stream();
    }

  /**
   * Removes a task from the repository by its unique identifier.
   *
   * @param id the unique identifier of the task to remove
   * @return the removed task
   * @throws IllegalArgumentException if no task exists for the specified ID
   */
  public T removeTaskById(final int id) {
        checkTaskExists(id);
        return taskStore.remove(id);
    }

  /**
   * Finds tasks that match the given filter criteria.
   *
   * @param filter the {@link Predicate} to apply to each {@code <T>}
   * @return a {@link Collection} of objects of type {@code <T>} that match the filter
   */
  public Collection<T> findTasksMatching(final Predicate<T> filter) {
        return taskStore.values().stream().filter(filter).toList();
    }

  /**
   * Removes tasks from the repository that satisfy the given filter condition.
   *
   * @param filter the {@link Predicate} used to identify objects of type {@code <T>} to remove
   */
  public void removeMatchingTasks(final Predicate<T> filter) {
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
