package com.tasktracker.task.store;

import com.tasktracker.cvs.TaskCsvMapper;
import com.tasktracker.cvs.exceptions.CvsMapperException;
import com.tasktracker.task.exception.ManagerSaveException;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class FileBakedTaskRepository extends InMemoryTaskRepository
    implements TaskRepository {
  private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private final Path dataFilePath;

  public FileBakedTaskRepository(Path dataFilePath) {
    this.dataFilePath = Objects.requireNonNull(dataFilePath, "Data file path can't be null.");
    ensureDataFileExists();
    loadFromFileToMemory();
  }

  @Override
  public <T extends Task> T addTask(final T task) {
    T addedTask = super.addTask(task);
    save();
    return addedTask;
  }

  private void ensureDataFileExists() throws ManagerSaveException {
    Objects.requireNonNull(dataFilePath, "Data file path can't be null");
    final Path parent = dataFilePath.getParent();
    try {
      if (parent != null) {
        Files.createDirectories(parent);
      }
      if (Files.notExists(dataFilePath)) {
        Files.createFile(dataFilePath);
        writeFile(TaskCsvMapper.CSV_HEADER + "\n");
      } else if (Files.size(dataFilePath) == 0) {
        writeFile(TaskCsvMapper.CSV_HEADER + "\n");
      }
    } catch (IOException e) {
      throw new ManagerSaveException("Failed to create or prepare task's file: " + dataFilePath, e);
    }
  }

  private void writeFile(final String content) throws ManagerSaveException {
    try (final var buff = Files.newBufferedWriter(dataFilePath, DEFAULT_CHARSET)) {
      buff.write(content);
    } catch (IOException e) {
      throw new ManagerSaveException("Failed to save tasks to file: " + dataFilePath, e);
    }
  }

  private void loadFromFileToMemory() throws ManagerSaveException {
    super.clearAllTasks();

    List<String> lines;
    try {
      lines = Files.readAllLines(dataFilePath, DEFAULT_CHARSET);
    } catch (IOException e) {
      if (Files.exists(dataFilePath)) {
        try {
          if (Files.size(dataFilePath)
              <= (TaskCsvMapper.CSV_HEADER.getBytes(DEFAULT_CHARSET).length
                  + System.lineSeparator().getBytes(DEFAULT_CHARSET).length)) {
            return;
          }
        } catch (IOException ex) {
          throw new ManagerSaveException(
              "Failed to read tasks from file (checking size failed): " + dataFilePath, ex);
        }
      }
      throw new ManagerSaveException("Failed to read tasks from file: " + dataFilePath, e);
    }

    if (lines.isEmpty() || !lines.get(0).trim().equals(TaskCsvMapper.CSV_HEADER.trim())) {
      if (!lines.isEmpty()) {
        System.err.println(
            "Warning: CSV file "
                + dataFilePath
                + " is missing a valid header. Starting with an empty repository.");
      }
      return;
    }

    List<Task> tasksFromFile =
        lines.stream()
            .skip(1)
            .filter(line -> !line.isBlank())
            .map(
                line -> {
                  try {
                    return TaskCsvMapper.fromCsv(line);
                  } catch (CvsMapperException e) {
                    System.err.println(
                        "Skipping malformed CSV line during load: ["
                            + line
                            + "]. Error: "
                            + e.getMessage());
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .toList();

    for (Task task : tasksFromFile) {
      try {
        super.addTask(task);
      } catch (IllegalArgumentException e) {
        System.err.println(
            "Error adding task from file (possible duplicate ID in CSV): "
                + task.getId()
                + " - "
                + e.getMessage());
      }
    }
  }

  private void save() throws ManagerSaveException {
    Collection<Task> tasksToSave = super.getAllTasks();
    final String csvContent =
        TaskCsvMapper.CSV_HEADER
            + "\n"
            + tasksToSave.stream().map(TaskCsvMapper::toCsv).collect(Collectors.joining("\n"));
    writeFile(csvContent);
  }

  @Override
  public Task updateTask(final Task updatedTask) throws TaskNotFoundException {
    final var result = super.updateTask(updatedTask);
    save();
    return result;
  }

  @Override
  public List<Task> getAllTasks() {
    return super.getAllTasks();
  }

  @Override
  public Optional<Task> getTaskById(UUID id) {
    return super.getTaskById(id);
  }

  @Override
  public Collection<Task> findTasksMatching(Predicate<Task> taskPredicate) {
    return super.findTasksMatching(taskPredicate);
  }

  @Override
  public Optional<Task> removeTask(final UUID id) {
    final var result = super.removeTask(id);
    if (result.isPresent()) {
      save();
    }
    return result;
  }

  @Override
  public boolean removeMatchingTasks(final Predicate<Task> taskPredicate) {
    final var result = super.removeMatchingTasks(taskPredicate);
    if (result) {
      save();
    }
    return result;
  }

  @Override
  public void clearAllTasks() {
    super.clearAllTasks();
    save();
  }
}
