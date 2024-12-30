package com.tasktracker.task.store;

import com.tasktracker.task.dto.EpicTaskCreationDTO;
import com.tasktracker.task.dto.RegularTaskCreationDTO;
import com.tasktracker.task.dto.SubTaskCreationDTO;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.util.*;
import java.util.function.Predicate;

/**
 * A repository for managing {@link Task} objects, providing operations for adding, updating,
 * retrieving, and removing tasks. Tasks are identified by unique integer IDs and stored in an
 * internal map.
 */
public final class InMemoryTaskRepository implements TaskRepository {
  public static final String TASK_CAN_T_BE_NULL = "Task can't be null";
  private final NavigableMap<Integer, Task> taskStore = new TreeMap<>();

  /**
   * Adds a new regular com.tasktracker.task to the repository based on the provided {@link
   * RegularTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the regular com.tasktracker.task to
   *     be created
   * @return the created {@link RegularTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  public RegularTask addTask(final RegularTaskCreationDTO dto) {
    Objects.requireNonNull(dto, TASK_CAN_T_BE_NULL);
    int id = generateId();
    RegularTask newTask = new RegularTask(id, dto.title(), dto.description(), TaskStatus.NEW);
    taskStore.put(id, newTask);
    return newTask;
  }

  /**
   * Adds a new epic com.tasktracker.task to the repository based on the provided {@link
   * EpicTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the epic com.tasktracker.task to be
   *     created
   * @return the created {@link EpicTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  public EpicTask addTask(final EpicTaskCreationDTO dto) {
    Objects.requireNonNull(dto, TASK_CAN_T_BE_NULL);
    int id = generateId();
    EpicTask newTask = new EpicTask(id, dto.title(), dto.description(), TaskStatus.NEW, Set.of());
    taskStore.put(id, newTask);
    return newTask;
  }

  /**
   * Adds a new sub-com.tasktracker.task to the repository based on the provided {@link
   * SubTaskCreationDTO}.
   *
   * @param dto the data transfer object containing details of the sub-com.tasktracker.task to be
   *     created
   * @return the created {@link SubTask} with an auto-generated unique ID
   * @throws NullPointerException if the provided {@code dto} is {@code null}
   */
  public SubTask addTask(final SubTaskCreationDTO dto) {
    Objects.requireNonNull(dto, TASK_CAN_T_BE_NULL);
    int id = generateId();
    SubTask newTask = new SubTask(id, dto.title(), dto.description(), TaskStatus.NEW, dto.epicId());
    taskStore.put(id, newTask);
    return newTask;
  }

  /**
   * Updates an existing com.tasktracker.task in the repository with new details. The
   * com.tasktracker.task to update must have a valid ID already existing in the repository.
   *
   * @param updatedTask the com.tasktracker.task containing the updated details, including a valid
   *     ID
   * @return the updated {@link Task} after applying the changes
   * @throws NullPointerException if the provided {@code updatedTask} is {@code null}
   * @throws NoSuchElementException if no com.tasktracker.task exists in the repository for the
   *     specified ID
   */
  public Task updateTask(final Task updatedTask) throws NoSuchElementException {
    Objects.requireNonNull(updatedTask, "Updated com.tasktracker.task can't be null");
    checkTaskExists(updatedTask.getId());
    taskStore.put(updatedTask.getId(), updatedTask);
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
   * Retrieves a com.tasktracker.task by its unique identifier.
   *
   * @param id the unique identifier of the com.tasktracker.task in the repository
   * @return an {@link Optional} containing the com.tasktracker.task if it exists, or an empty
   *     {@link Optional} if it does not
   */
  public Optional<Task> getTaskById(final int id) {
    return Optional.ofNullable(taskStore.get(id));
  }

  /**
   * Removes a com.tasktracker.task from the repository by its unique identifier.
   *
   * @param id the unique identifier of the com.tasktracker.task to remove from the repository
   * @return an {@link Optional} containing the removed com.tasktracker.task, or an empty {@link
   *     Optional} if no com.tasktracker.task was found for the given ID
   * @throws IllegalArgumentException if no com.tasktracker.task exists for the specified ID
   */
  public Optional<Task> removeTaskById(final int id) {
    checkTaskExists(id);
    return Optional.ofNullable(taskStore.remove(id));
  }

  /**
   * Finds tasks that match the given taskPredicate criteria.
   *
   * @param taskPredicate the {@link Predicate} to apply to each com.tasktracker.task for filtering
   * @return a {@link Collection} of tasks that satisfy the given {@link Predicate}; an empty list
   *     if no such tasks exist
   */
  public Collection<Task> findTasksMatching(final Predicate<Task> taskPredicate) {
    return taskStore.values().stream().filter(taskPredicate).toList();
  }

  /**
   * Removes tasks from the repository that satisfy the given taskPredicate condition.
   *
   * @param taskPredicate the {@link Predicate} used to identify tasks to remove
   */
  public boolean removeMatchingTasks(final Predicate<Task> taskPredicate) {
    Objects.requireNonNull(taskPredicate);
    return taskStore.entrySet().removeIf(entry -> taskPredicate.test(entry.getValue()));
  }

  /**
   * Clears all tasks from the repository, permanently deleting all stored data. After this
   * operation is performed, the repository will be empty. This action is irreversible.
   */
  public void clearAllTasks() {
    taskStore.clear();
  }

  /**
   * Verifies that a com.tasktracker.task with the specified ID exists in the repository.
   *
   * @param id the unique identifier of the com.tasktracker.task
   * @throws NoSuchElementException if no com.tasktracker.task with the specified ID exists
   */
  private void checkTaskExists(final int id) throws NoSuchElementException {
    if (!taskStore.containsKey(id)) {
      throw new NoSuchElementException("Task ID" + id + " does not exist.");
    }
  }

  /**
   * Generates a unique integer ID for new tasks. The ID is calculated as one greater than the
   * highest current key in the repository. If the repository is empty, the ID will start at 0.
   *
   * @return a unique integer ID for the new com.tasktracker.task
   */
  private int generateId() {
    return taskStore.isEmpty() ? 0 : taskStore.lastKey() + 1;
  }
}
