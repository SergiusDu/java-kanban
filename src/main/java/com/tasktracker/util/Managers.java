package com.tasktracker.util;

import com.tasktracker.task.manager.HistoryManager;
import com.tasktracker.task.manager.InMemoryHistoryManager;
import com.tasktracker.task.manager.InMemoryTaskManager;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.store.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for creating and managing instances of core components like {@link TaskManager} and
 * {@link HistoryManager}.
 */
public class Managers {
  private static final Path DATA_FILE_PATH = Paths.get("data", "task_data.cvs");

  private Managers() {}

  /**
   * Creates and returns the default implementation of {@link TaskManager}. This default instance
   * uses in-memory repositories for task and history management, and is linked to an in-memory
   * history manager for handling task history.
   *
   * @return an instance of {@link InMemoryTaskManager} configured with in-memory task and history
   *     repositories
   */
  public static TaskManager getDefault() {
    TaskRepository taskRepository = new FileBakedTaskRepository(DATA_FILE_PATH);
    HistoryRepository historyRepository = new InMemoryHistoryRepository();
    HistoryManager historyManager = new InMemoryHistoryManager(historyRepository);
    return new InMemoryTaskManager(taskRepository, historyManager);
  }
}
