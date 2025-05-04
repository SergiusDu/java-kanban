package com.tasktracker.task.store;

import com.tasktracker.cvs.TaskCsvMapper;
import com.tasktracker.task.exception.ManagerSaveException;
import com.tasktracker.task.model.implementations.Task;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A task repository implementation that persists tasks to a CSV file. This repository extends
 * InMemoryTaskRepository and adds file-based storage functionality, automatically saving changes to
 * disk after each modification operation.
 */
public final class FileBakedTaskRepository extends InMemoryTaskRepository
    implements TaskRepository {
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private final Path dataFilePath;

  /**
   * Creates a new FileBakedTaskRepository that stores tasks in the specified CSV file. Tasks will
   * be loaded from the file if it exists, or the file will be created if it doesn't exist.
   *
   * @param dataFilePath Path to the CSV file for task persistence
   * @throws ManagerSaveException if there are errors creating/accessing the file or loading tasks
   * @throws NullPointerException if dataFilePath is null
   */
  public FileBakedTaskRepository(Path dataFilePath) {
    this.dataFilePath = Objects.requireNonNull(dataFilePath, "Data file path can't be null.");
    ensureDataFileExists();
    load();
  }

  /**
   * Ensures that the data file and its parent directories exist, creating them if necessary.
   *
   * <p>This method performs the following:
   *
   * <ul>
   *   <li>Validates that dataFilePath is not null
   *   <li>Creates any missing parent directories recursively
   *   <li>Creates the target data file if it doesn't exist
   * </ul>
   *
   * @throws ManagerSaveException if an I/O error occurs when creating directories or file
   * @throws NullPointerException if dataFilePath is null
   */
  private void ensureDataFileExists() throws ManagerSaveException {
    Objects.requireNonNull(dataFilePath, "Data file path can't be null");
    final Path parent = dataFilePath.getParent();
    try {
      Files.createDirectories(parent);
      if (Files.notExists(dataFilePath)) {
        Files.createFile(dataFilePath);
      }
    } catch (IOException e) {
      throw new ManagerSaveException("Failed to create task's file.", e);
    }
  }

  /**
   * Writes the provided content to the repository's data file.
   *
   * @param content the string content to write to the file
   * @throws ManagerSaveException if writing to the file fails
   */
  private void writeFile(final String content) throws ManagerSaveException {
    try (final var buff = Files.newBufferedWriter(dataFilePath, DEFAULT_CHARSET)) {
      buff.write(content);
    } catch (IOException e) {
      throw new ManagerSaveException("Failed to save tasks to file.", e);
    }
  }

  /**
   * Loads tasks from the data file into memory. Skips the header line and converts each CSV line to
   * a Task object.
   *
   * @throws ManagerSaveException if reading from the file fails
   */
  private void load() throws ManagerSaveException {
    try (final var buff = Files.newBufferedReader(dataFilePath, DEFAULT_CHARSET)) {
      buff.lines().skip(1).map(TaskCsvMapper::fromCvs).forEach(super::addTask);
    } catch (IOException e) {
      throw new ManagerSaveException("Failed to load tasks from file", e);
    }
  }

  /**
   * Saves all tasks from memory to the data file in CSV format. Includes a header line followed by
   * task data.
   *
   * @throws ManagerSaveException if saving to the file fails
   */
  private void save() throws ManagerSaveException {
    final String csvContent =
        TaskCsvMapper.CSV_HEADER
            + "\n"
            + super.getAllTasks().stream()
                .map(TaskCsvMapper::toCvs)
                .collect(Collectors.joining("\n"));
    writeFile(csvContent);
  }

  /**
   * {@inheritDoc} The task is also persisted to the data file.
   *
   * @throws ManagerSaveException if saving to the file fails
   */
  @Override
  public <T extends Task> T addTask(final T task) {
    var result = super.addTask(task);
    save();
    return result;
  }

  /**
   * {@inheritDoc} The updated task is also persisted to the data file.
   *
   * @throws ManagerSaveException if saving to the file fails
   */
  @Override
  public Task updateTask(final Task updatedTask) {
    final var result = super.updateTask(updatedTask);
    save();
    return result;
  }

  @Override
  public Collection<Task> getAllTasks() {
    load();
    return super.getAllTasks();
  }

  @Override
  public Optional<Task> getTaskById(int id) {
    load();
    return super.getTaskById(id);
  }

  /**
   * {@inheritDoc} The removal is also persisted to the data file.
   *
   * @throws ManagerSaveException if saving to the file fails
   */
  @Override
  public Optional<Task> removeTask(final int id) {
    final var result = super.removeTask(id);
    save();
    return result;
  }

  @Override
  public Collection<Task> findTasksMatching(Predicate<Task> taskPredicate) {
    load();
    return super.findTasksMatching(taskPredicate);
  }

  /**
   * {@inheritDoc} The removal is also persisted to the data file.
   *
   * @throws ManagerSaveException if saving to the file fails
   */
  @Override
  public boolean removeMatchingTasks(final Predicate<Task> taskPredicate) {
    final var result = super.removeMatchingTasks(taskPredicate);
    save();
    return result;
  }

  /**
   * {@inheritDoc} The cleared state is also persisted to the data file.
   *
   * @throws ManagerSaveException if saving to the file fails
   */
  @Override
  public void clearAllTasks() {
    super.clearAllTasks();
    save();
  }
}
