package com.tasktracker.util;

import com.tasktracker.task.manager.HistoryManager;
import com.tasktracker.task.manager.InMemoryHistoryManager;
import com.tasktracker.task.manager.InMemoryTaskManager;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.store.HistoryRepository;
import com.tasktracker.task.store.InMemoryHistoryRepository;
import com.tasktracker.task.store.InMemoryTaskRepository;
import com.tasktracker.task.store.TaskRepository;

/**
 * Utility class for creating and managing instances of core components like {@link TaskManager} and
 * {@link HistoryManager}.
 */
public class Managers {

  private Managers() {}

  /**
   * Creates and returns the default {@link TaskManager} instance with an in-memory task and history
   * storage. The history is managed using an {@link InMemoryHistoryManager} with a predefined
   * limit.
   *
   * @return the default {@link TaskManager} instance
   */
  public static TaskManager getDefault() {
    TaskRepository taskRepository = new InMemoryTaskRepository();
    HistoryRepository historyRepository = new InMemoryHistoryRepository();
    int historyLimit = 10;
    HistoryManager historyManager = new InMemoryHistoryManager(historyRepository, historyLimit);
    return new InMemoryTaskManager(taskRepository, historyManager);
  }
}
