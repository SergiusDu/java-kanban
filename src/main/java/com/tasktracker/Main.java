package com.tasktracker;

import com.tasktracker.task.dto.EpicTaskCreationDTO;
import com.tasktracker.task.dto.RegularTaskCreationDTO;
import com.tasktracker.task.dto.RegularTaskUpdateDTO;
import com.tasktracker.task.dto.SubTaskCreationDTO;
import com.tasktracker.task.dto.SubTaskUpdateDTO;
import com.tasktracker.task.manager.HistoryManager;
import com.tasktracker.task.manager.InMemoryHistoryManager;
import com.tasktracker.task.manager.InMemoryTaskManager;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.HistoryRepository;
import com.tasktracker.task.store.InMemoryHistoryRepository;
import com.tasktracker.task.store.InMemoryTaskRepository;
import com.tasktracker.task.store.TaskRepository;
import java.util.Optional;

public class Main {
  public static void main(String[] args) {
    // Run different test sets
    testBasicCrudOperations();
    testAdditionalEpicScenarios();
    testRemovingTasks();
    testUpdateEpicAndSubtaskStatus();
    testRemoveTasksByType();
    testBoundaryCases();
  }

  /** Basic CRUD operations: creation, reading, updating, printing. */
  private static void testBasicCrudOperations() {
    System.out.println("===== testBasicCrudOperations =====");
    TaskRepository tr = new InMemoryTaskRepository();
    HistoryRepository hr = new InMemoryHistoryRepository();
    HistoryManager hm = new InMemoryHistoryManager(tr, hr, 10);
    TaskManager tm = new InMemoryTaskManager(tr, hm);

    // Create two regular tasks
    RegularTask regTask1 =
        tm.addTask(
            new RegularTaskCreationDTO("RegularTask #1 Title", "Description #1 is longer than 10"));
    RegularTask regTask2 =
        tm.addTask(
            new RegularTaskCreationDTO(
                "RegularTask #2 Title", "Description #2 is definitely longer"));

    // Create an epic and a subtask
    EpicTask epic1 =
        tm.addTask(new EpicTaskCreationDTO("Epic #1 Title Enough", "Epic #1 Desc is bigger"));
    SubTask sub1 =
        tm.addTask(
            new SubTaskCreationDTO("SubTask #1 Title", "SubTask #1 Description", epic1.getId()));

    // Print all
    System.out.println("Initial tasks:");
    printAll(tm);

    // Update tasks
    tm.updateTask(
        new RegularTaskUpdateDTO(
            regTask1.getId(),
            "RegularTask #1 Title UPDATED",
            "New description long enough #1",
            TaskStatus.IN_PROGRESS));
    tm.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            "SubTask #1 Title updated",
            "SubTask #1 new description is enough",
            TaskStatus.DONE,
            epic1.getId()));

    // Print again
    System.out.println("After updates:");
    printAll(tm);

    // Check one of the tasks by ID
    Optional<Task> maybeReg2 = tm.getTaskById(regTask2.getId());
    System.out.println("Check getTaskById for RegularTask2: " + maybeReg2);

    System.out.println("====================================\n");
  }

  /** Additional epic scenarios: multiple subtasks, updates, and validations. */
  private static void testAdditionalEpicScenarios() {
    System.out.println("===== testAdditionalEpicScenarios =====");
    TaskRepository tr = new InMemoryTaskRepository();
    HistoryRepository hr = new InMemoryHistoryRepository();
    HistoryManager hm = new InMemoryHistoryManager(tr, hr, 10);
    TaskManager tm = new InMemoryTaskManager(tr, hm);

    EpicTask epicA =
        tm.addTask(new EpicTaskCreationDTO("Epic A Title Enough", "Epic A Description Enough"));
    SubTask subA1 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubA1 Title Enough", "SubA1 Desc definitely > 10", epicA.getId()));
    SubTask subA2 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubA2 Title Enough", "SubA2 Desc definitely > 10", epicA.getId()));

    System.out.println("All tasks after creation:");
    printAll(tm);

    // Update both subtasks to DONE
    tm.updateTask(
        new SubTaskUpdateDTO(
            subA1.getId(),
            "SubA1 Title new",
            "SubA1 Desc is still > 10 chars",
            TaskStatus.DONE,
            epicA.getId()));
    tm.updateTask(
        new SubTaskUpdateDTO(
            subA2.getId(),
            "SubA2 Title new",
            "SubA2 Desc is still > 10 chars",
            TaskStatus.DONE,
            epicA.getId()));

    System.out.println("After setting all subtasks to DONE:");
    printAll(tm);

    // Epic should be DONE
    System.out.println("Epic A should now have status DONE");
    System.out.println("====================================\n");
  }

  /** Removing tasks one by one, including repeated deletions. */
  private static void testRemovingTasks() {
    System.out.println("===== testRemovingTasks =====");
    TaskRepository tr = new InMemoryTaskRepository();
    HistoryRepository hr = new InMemoryHistoryRepository();
    HistoryManager hm = new InMemoryHistoryManager(tr, hr, 10);
    TaskManager tm = new InMemoryTaskManager(tr, hm);

    // Create a regular task
    RegularTask regTaskA =
        tm.addTask(
            new RegularTaskCreationDTO("Regular A Title Enough", "Regular A Description Enough"));

    // Create an epic with two subtasks
    EpicTask epicA =
        tm.addTask(new EpicTaskCreationDTO("EpicA Title Enough", "EpicA Description Enough"));
    SubTask subA1 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubA1 Title Enough", "SubA1 Desc is more than 10 chars", epicA.getId()));
    SubTask subA2 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubA2 Title Enough", "SubA2 Desc is more than 10 chars", epicA.getId()));

    System.out.println("Tasks before removing anything:");
    printAll(tm);

    // Remove regular
    System.out.println("Removing regularTask with ID=" + regTaskA.getId());
    tm.removeTaskById(regTaskA.getId());
    printAll(tm);

    // Remove epic (this should also remove subtasks)
    System.out.println("Removing epic with ID=" + epicA.getId());
    tm.removeTaskById(epicA.getId());
    printAll(tm);

    // Attempt to remove the same task again
    System.out.println("Removing the same regularTask again: " + regTaskA.getId());
    try {
      tm.removeTaskById(regTaskA.getId());
      System.out.println("No exception? Possibly Optional.empty() from store");
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }

    System.out.println("====================================\n");
  }

  /** Update epic and subtask statuses, ensuring epic status is recalculated properly. */
  private static void testUpdateEpicAndSubtaskStatus() {
    System.out.println("===== testUpdateEpicAndSubtaskStatus =====");
    TaskRepository tr = new InMemoryTaskRepository();
    HistoryRepository hr = new InMemoryHistoryRepository();
    HistoryManager hm = new InMemoryHistoryManager(tr, hr, 10);
    TaskManager tm = new InMemoryTaskManager(tr, hm);

    EpicTask epicB =
        tm.addTask(new EpicTaskCreationDTO("EpicB Title Enough", "EpicB Description Enough"));
    SubTask subB1 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubB1 Title Enough", "SubB1 Desc is definitely > 10 chars", epicB.getId()));
    SubTask subB2 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubB2 Title Enough", "SubB2 Desc is definitely > 10 chars", epicB.getId()));

    System.out.println("Initial tasks:");
    printAll(tm);

    // subB1 -> DONE
    tm.updateTask(
        new SubTaskUpdateDTO(
            subB1.getId(),
            "SubB1 Title updated",
            "Desc updated still over 10 chars",
            TaskStatus.DONE,
            epicB.getId()));
    System.out.println("After subB1 -> DONE:");
    printAll(tm);
    System.out.println("EpicB status should be IN_PROGRESS now.");

    // subB2 -> DONE
    tm.updateTask(
        new SubTaskUpdateDTO(
            subB2.getId(),
            "SubB2 Title updated",
            "Desc updated still over 10 chars",
            TaskStatus.DONE,
            epicB.getId()));
    System.out.println("After subB2 -> DONE:");
    printAll(tm);
    System.out.println("EpicB status should be DONE now.");
    System.out.println("====================================\n");
  }

  /** Removing tasks by class type. */
  private static void testRemoveTasksByType() {
    System.out.println("===== testRemoveTasksByType =====");
    TaskRepository tr = new InMemoryTaskRepository();
    HistoryRepository hr = new InMemoryHistoryRepository();
    HistoryManager hm = new InMemoryHistoryManager(tr, hr, 10);
    TaskManager tm = new InMemoryTaskManager(tr, hm);

    // Create multiple tasks of different types
    RegularTask reg1 =
        tm.addTask(
            new RegularTaskCreationDTO(
                "RemoveByType #1 Title", "RemoveByType #1 Desc is definitely longer"));
    EpicTask epicC =
        tm.addTask(
            new EpicTaskCreationDTO("RemoveByType EpicC Title", "RemoveByType EpicC Desc is long"));
    SubTask subC1 =
        tm.addTask(
            new SubTaskCreationDTO(
                "SubC1 Title Enough", "SubC1 Desc definitely > 10", epicC.getId()));

    System.out.println("All tasks before remove by type: ");
    printAll(tm);

    // Remove all SubTasks
    System.out.println("Removing all SubTasks by type:");
    tm.removeTasksByType(SubTask.class);
    printAll(tm);

    // Remove all RegularTasks
    System.out.println("Removing all RegularTasks by type:");
    tm.removeTasksByType(RegularTask.class);
    printAll(tm);

    // Remove all Epics
    System.out.println("Removing all Epics by type:");
    tm.removeTasksByType(EpicTask.class);
    printAll(tm);

    System.out.println("====================================\n");
  }

  /**
   * Testing boundary cases (e.g., repeated creation or short titles). This method includes examples
   * of invalid inputs to show how ValidationException is thrown.
   */
  private static void testBoundaryCases() {
    System.out.println("===== testBoundaryCases =====");
    TaskRepository tr = new InMemoryTaskRepository();
    HistoryRepository hr = new InMemoryHistoryRepository();
    HistoryManager hm = new InMemoryHistoryManager(tr, hr, 10);
    TaskManager tm = new InMemoryTaskManager(tr, hm);

    // Valid tasks
    RegularTask regValid =
        tm.addTask(
            new RegularTaskCreationDTO("Boundary Check Title", "Boundary Check Description"));
    System.out.println("Created a valid RegularTask: " + regValid);

    // Attempt to create invalid tasks (should throw ValidationException)
    try {
      tm.addTask(new RegularTaskCreationDTO("Short", "Desc is longer but title is short"));
    } catch (Exception e) {
      System.out.println("Caught exception for short title: " + e.getMessage());
    }

    try {
      tm.addTask(new SubTaskCreationDTO("Sub with short", "Tiny", 999));
    } catch (Exception e) {
      System.out.println("Caught exception for short description: " + e.getMessage());
    }
    try {
      tm.addTask(new EpicTaskCreationDTO("Epic short", "Desc short"));
    } catch (Exception e) {
      System.out.println("Caught exception for both short title & desc: " + e.getMessage());
    }

    // Print final state
    System.out.println("Final state in testBoundaryCases:");
    printAll(tm);
    System.out.println("====================================\n");
  }

  /** Utility method to print all tasks. */
  private static void printAll(TaskManager tm) {
    System.out.println("--- All tasks in the repository ---");
    tm.getAllTasks().forEach(System.out::println);
    System.out.println("-----------------------------------\n");
  }
}
