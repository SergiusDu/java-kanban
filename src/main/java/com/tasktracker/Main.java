package com.tasktracker;

import com.tasktracker.task.dto.EpicTaskCreationDTO;
import com.tasktracker.task.dto.RegularTaskCreationDTO;
import com.tasktracker.task.dto.RegularTaskUpdateDTO;
import com.tasktracker.task.dto.SubTaskCreationDTO;
import com.tasktracker.task.dto.SubTaskUpdateDTO;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.manager.TaskManager;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import com.tasktracker.task.store.exception.TaskNotFoundException;
import com.tasktracker.util.Managers;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class Main {

  private static final String TEST_SECTION_START_FORMAT = "===== %s =====";
  private static final String TEST_SECTION_END = "====================================\n";

  private static <T extends Task> T findAddedTask(
      TaskManager tm, Class<T> taskClass, Set<UUID> idsBefore) {
    Set<UUID> idsAfter =
        tm.getAllTasksByClass(taskClass).stream().map(Task::getId).collect(Collectors.toSet());
    idsAfter.removeAll(idsBefore);
    if (idsAfter.size() != 1) {
      throw new IllegalStateException(
          "Expected to find exactly one new task of type "
              + taskClass.getSimpleName()
              + " but found "
              + idsAfter.size()
              + ". IDs before: "
              + idsBefore
              + ", IDs after (all): "
              + tm.getAllTasksByClass(taskClass).stream()
                  .map(Task::getId)
                  .collect(Collectors.toSet()));
    }
    UUID newId = idsAfter.iterator().next();
    return tm.getAllTasksByClass(taskClass).stream()
        .filter(t -> t.getId().equals(newId))
        .findFirst()
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Could not retrieve added "
                        + taskClass.getSimpleName()
                        + " with ID: "
                        + newId));
  }

  private static void printTestSectionHeader(String testName) {
    System.out.printf((TEST_SECTION_START_FORMAT) + "%n", testName);
  }

  public static void main(String[] args) throws ValidationException, TaskNotFoundException {
    testUserScenario();
    testBasicCrudOperations();
    testAdditionalEpicScenarios();
    testRemovingTasks();
    testUpdateEpicAndSubtaskStatus();
    testRemoveTasksByType();
    testBoundaryCases();
  }

  private static void testUserScenario() throws ValidationException, TaskNotFoundException {
    printTestSectionHeader("testUserScenario");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeRegular1 =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "Regular Task 1 UserScenario",
            "Regular task 1 user scenario description is long enough",
            null,
            null));
    RegularTask regularTask1 = findAddedTask(tm, RegularTask.class, idsBeforeRegular1);

    Set<UUID> idsBeforeRegular2 =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "Regular Task 2 UserScenario",
            "Regular task 2 user scenario description is long enough",
            null,
            null));
    RegularTask regularTask2 = findAddedTask(tm, RegularTask.class, idsBeforeRegular2);

    Set<UUID> idsBeforeEpic1 =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO(
            "Epic With Subtasks UserScenario",
            "Epic with three subtasks user scenario description",
            null));
    EpicTask epicWithSubtasks = findAddedTask(tm, EpicTask.class, idsBeforeEpic1);

    Set<UUID> idsBeforeSub1 =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "Subtask 1 UserScenario",
            "Subtask 1 user scenario description is long enough",
            epicWithSubtasks.getId(),
            null,
            null));
    SubTask subTask1 = findAddedTask(tm, SubTask.class, idsBeforeSub1);

    Set<UUID> idsBeforeSub2 =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "Subtask 2 UserScenario",
            "Subtask 2 user scenario description is long enough",
            epicWithSubtasks.getId(),
            null,
            null));
    SubTask subTask2 = findAddedTask(tm, SubTask.class, idsBeforeSub2);

    Set<UUID> idsBeforeSub3 =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "Subtask 3 UserScenario",
            "Subtask 3 user scenario description is long enough",
            epicWithSubtasks.getId(),
            null,
            null));
    SubTask subTask3 = findAddedTask(tm, SubTask.class, idsBeforeSub3);

    Set<UUID> idsBeforeEpic2 =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO(
            "Epic Without Subtasks UserScenario",
            "Epic without any subtasks user scenario description",
            null));
    EpicTask epicWithoutSubtasks = findAddedTask(tm, EpicTask.class, idsBeforeEpic2);

    tm.getTask(regularTask1.getId());
    printHistory(tm);
    tm.getTask(epicWithSubtasks.getId());
    printHistory(tm);
    tm.getTask(subTask2.getId());
    printHistory(tm);
    tm.getTask(regularTask2.getId());
    printHistory(tm);
    tm.getTask(epicWithoutSubtasks.getId());
    printHistory(tm);
    tm.getTask(subTask1.getId());
    printHistory(tm);
    tm.getTask(subTask3.getId());
    printHistory(tm);
    tm.getTask(epicWithSubtasks.getId());
    printHistory(tm);

    System.out.println("Final history (no duplicates, epicWithSubtasks should be last):");
    printHistory(tm);

    tm.removeTaskById(regularTask1.getId());
    System.out.println("History after deleting Regular Task 1:");
    printHistory(tm);

    tm.removeTaskById(epicWithSubtasks.getId());
    System.out.println("History after deleting epic with subtasks:");
    printHistory(tm);
    System.out.print(TEST_SECTION_END);
  }

  private static void printHistory(TaskManager tm) {
    System.out.print("History: ");
    if (tm.getHistory().isEmpty()) {
      System.out.print("empty");
    } else {
      tm.getHistory().forEach(task -> System.out.print(task.getId() + " "));
    }
    System.out.println();
  }

  private static void testBasicCrudOperations() throws ValidationException, TaskNotFoundException {
    printTestSectionHeader("testBasicCrudOperations");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeReg1_CRUD =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "RegularTask #1 CRUD Title", "Description #1 CRUD is longer than 10", null, null));
    RegularTask regTask1 = findAddedTask(tm, RegularTask.class, idsBeforeReg1_CRUD);

    Set<UUID> idsBeforeReg2_CRUD =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "RegularTask #2 CRUD Title", "Description #2 CRUD is definitely longer", null, null));
    RegularTask regTask2 = findAddedTask(tm, RegularTask.class, idsBeforeReg2_CRUD);

    Set<UUID> idsBeforeEpic_CRUD =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO("Epic #1 CRUD Title Enough", "Epic #1 CRUD Desc is bigger", null));
    EpicTask epic1 = findAddedTask(tm, EpicTask.class, idsBeforeEpic_CRUD);

    Set<UUID> idsBeforeSub_CRUD =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubTask #1 CRUD Title", "SubTask #1 CRUD Description", epic1.getId(), null, null));
    SubTask sub1 = findAddedTask(tm, SubTask.class, idsBeforeSub_CRUD);

    System.out.println("Initial tasks:");
    printAll(tm);

    tm.updateTask(
        new RegularTaskUpdateDTO(
            regTask1.getId(),
            "RegularTask #1 CRUD Title UPDATED",
            "New CRUD description long enough #1",
            TaskStatus.IN_PROGRESS,
            null,
            null));
    tm.updateTask(
        new SubTaskUpdateDTO(
            sub1.getId(),
            "SubTask #1 CRUD Title updated",
            "SubTask #1 new CRUD description is enough",
            TaskStatus.DONE,
            epic1.getId(),
            null,
            null));

    System.out.println("After updates:");
    printAll(tm);

    Optional<Task> maybeReg2 = tm.getTask(regTask2.getId());
    System.out.println("Check getTaskById for RegularTask2: " + maybeReg2);

    System.out.print(TEST_SECTION_END);
  }

  private static void testAdditionalEpicScenarios()
      throws ValidationException, TaskNotFoundException {
    printTestSectionHeader("testAdditionalEpicScenarios");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeEpicA =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO(
            "Epic A Scenarios Title Enough", "Epic A Scenarios Description Enough", null));
    EpicTask epicA = findAddedTask(tm, EpicTask.class, idsBeforeEpicA);

    Set<UUID> idsBeforeSubA1 =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubA1 Scenarios Title Enough",
            "SubA1 Scenarios Desc definitely > 10",
            epicA.getId(),
            null,
            null));
    SubTask subA1 = findAddedTask(tm, SubTask.class, idsBeforeSubA1);

    Set<UUID> idsBeforeSubA2 =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubA2 Scenarios Title Enough",
            "SubA2 Scenarios Desc definitely > 10",
            epicA.getId(),
            null,
            null));
    SubTask subA2 = findAddedTask(tm, SubTask.class, idsBeforeSubA2);

    System.out.println("All tasks after creation:");
    printAll(tm);

    tm.updateTask(
        new SubTaskUpdateDTO(
            subA1.getId(),
            "SubA1 Scenarios Title new",
            "SubA1 Scenarios Desc is still > 10 chars",
            TaskStatus.DONE,
            epicA.getId(),
            null,
            null));
    tm.updateTask(
        new SubTaskUpdateDTO(
            subA2.getId(),
            "SubA2 Scenarios Title new",
            "SubA2 Scenarios Desc is still > 10 chars",
            TaskStatus.DONE,
            epicA.getId(),
            null,
            null));

    System.out.println("After setting all subtasks to DONE:");
    printAll(tm);

    System.out.println("Epic A should now have status DONE");
    Optional<Task> updatedEpicA = tm.getTask(epicA.getId());
    updatedEpicA.ifPresent(task -> System.out.println("Actual Epic A status: " + task.getStatus()));

    System.out.print(TEST_SECTION_END);
  }

  private static void testRemovingTasks() throws ValidationException, TaskNotFoundException {
    printTestSectionHeader("testRemovingTasks");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeRegA_Remove =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "Regular A Remove Title Enough", "Regular A Remove Description Enough", null, null));
    RegularTask regTaskA = findAddedTask(tm, RegularTask.class, idsBeforeRegA_Remove);

    Set<UUID> idsBeforeEpicA_Remove =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO(
            "EpicA Remove Title Enough", "EpicA Remove Description Enough", null));
    EpicTask epicA = findAddedTask(tm, EpicTask.class, idsBeforeEpicA_Remove);

    Set<UUID> idsBeforeSubA1_Remove =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubA1 Remove Title Enough",
            "SubA1 Remove Desc is more than 10 chars",
            epicA.getId(),
            null,
            null));
    SubTask subA1 = findAddedTask(tm, SubTask.class, idsBeforeSubA1_Remove);

    Set<UUID> idsBeforeSubA2_Remove =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubA2 Remove Title Enough",
            "SubA2 Remove Desc is more than 10 chars",
            epicA.getId(),
            null,
            null));
    findAddedTask(tm, SubTask.class, idsBeforeSubA2_Remove);

    System.out.println("Tasks before removing anything:");
    printAll(tm);

    System.out.println("Removing regularTask with ID=" + regTaskA.getId());
    tm.removeTaskById(regTaskA.getId());
    printAll(tm);

    System.out.println("Removing epic with ID=" + epicA.getId());
    tm.removeTaskById(epicA.getId());
    printAll(tm);
    System.out.println("Subtask subA1 should be removed: " + tm.getTask(subA1.getId()).isEmpty());

    System.out.println("Attempting to remove the same regularTask again: " + regTaskA.getId());
    Optional<Task> removedAgain = tm.removeTaskById(regTaskA.getId());
    if (removedAgain.isEmpty()) {
      System.out.println("Correctly returned empty Optional for non-existent task.");
    } else {
      System.out.println("Error: Should have returned empty Optional.");
    }

    System.out.print(TEST_SECTION_END);
  }

  private static void testUpdateEpicAndSubtaskStatus()
      throws ValidationException, TaskNotFoundException {
    printTestSectionHeader("testUpdateEpicAndSubtaskStatus");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeEpicB_Update =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO(
            "EpicB Update Title Enough", "EpicB Update Description Enough", null));
    EpicTask epicB = findAddedTask(tm, EpicTask.class, idsBeforeEpicB_Update);

    Set<UUID> idsBeforeSubB1_Update =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubB1 Update Title Enough",
            "SubB1 Update Desc is definitely > 10 chars",
            epicB.getId(),
            null,
            null));
    SubTask subB1 = findAddedTask(tm, SubTask.class, idsBeforeSubB1_Update);

    Set<UUID> idsBeforeSubB2_Update =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubB2 Update Title Enough",
            "SubB2 Update Desc is definitely > 10 chars",
            epicB.getId(),
            null,
            null));
    SubTask subB2 = findAddedTask(tm, SubTask.class, idsBeforeSubB2_Update);

    System.out.println("Initial tasks:");
    printAll(tm);
    System.out.println("Initial EpicB status: " + tm.getTask(epicB.getId()).map(Task::getStatus));

    tm.updateTask(
        new SubTaskUpdateDTO(
            subB1.getId(),
            "SubB1 Update Title updated",
            "Desc updated still over 10 chars",
            TaskStatus.DONE,
            epicB.getId(),
            null,
            null));
    System.out.println("After subB1 -> DONE:");
    printAll(tm);
    System.out.println(
        "EpicB status should be IN_PROGRESS now. Actual: "
            + tm.getTask(epicB.getId()).map(Task::getStatus));

    tm.updateTask(
        new SubTaskUpdateDTO(
            subB2.getId(),
            "SubB2 Update Title updated",
            "Desc updated still over 10 chars",
            TaskStatus.DONE,
            epicB.getId(),
            null,
            null));
    System.out.println("After subB2 -> DONE:");
    printAll(tm);
    System.out.println(
        "EpicB status should be DONE now. Actual: "
            + tm.getTask(epicB.getId()).map(Task::getStatus));
    System.out.print(TEST_SECTION_END);
  }

  private static void testRemoveTasksByType() throws ValidationException, TaskNotFoundException {
    printTestSectionHeader("testRemoveTasksByType");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeReg1_Type =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "RemoveByType #1 Title", "RemoveByType #1 Desc is definitely longer", null, null));
    findAddedTask(tm, RegularTask.class, idsBeforeReg1_Type);

    Set<UUID> idsBeforeEpicC_Type =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO(
            "RemoveByType EpicC Title", "RemoveByType EpicC Desc is long", null));
    EpicTask epicC = findAddedTask(tm, EpicTask.class, idsBeforeEpicC_Type);

    Set<UUID> idsBeforeSubC1_Type =
        tm.getAllTasksByClass(SubTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new SubTaskCreationDTO(
            "SubC1 Type Title Enough",
            "SubC1 Type Desc definitely > 10",
            epicC.getId(),
            null,
            null));
    findAddedTask(tm, SubTask.class, idsBeforeSubC1_Type);

    System.out.println("All tasks before remove by type: ");
    printAll(tm);

    System.out.println("Removing all SubTasks by type:");
    tm.removeTasksByType(SubTask.class);
    printAll(tm);
    System.out.println(
        "EpicC status after removing its subtasks: "
            + tm.getTask(epicC.getId()).map(Task::getStatus));

    System.out.println("Removing all RegularTasks by type:");
    tm.removeTasksByType(RegularTask.class);
    printAll(tm);

    System.out.println("Removing all Epics by type:");
    tm.removeTasksByType(EpicTask.class);
    printAll(tm);

    System.out.print(TEST_SECTION_END);
  }

  private static void testBoundaryCases() throws ValidationException {
    printTestSectionHeader("testBoundaryCases");
    TaskManager tm = Managers.getDefault();

    Set<UUID> idsBeforeRegValid_Boundary =
        tm.getAllTasksByClass(RegularTask.class).stream()
            .map(Task::getId)
            .collect(Collectors.toSet());
    tm.addTask(
        new RegularTaskCreationDTO(
            "Boundary Check Title Valid", "Boundary Check Description Valid", null, null));
    RegularTask regValid = findAddedTask(tm, RegularTask.class, idsBeforeRegValid_Boundary);
    System.out.println("Created a valid RegularTask: " + regValid);

    try {
      tm.addTask(
          new RegularTaskCreationDTO("Short", "Desc is longer but title is short", null, null));
    } catch (ValidationException e) {
      System.out.println("Caught expected ValidationException for short title: " + e.getMessage());
    }

    Set<UUID> idsBeforeDummyEpic =
        tm.getAllTasksByClass(EpicTask.class).stream().map(Task::getId).collect(Collectors.toSet());
    tm.addTask(
        new EpicTaskCreationDTO("Dummy Epic For Boundary Subtask", "Dummy epic description", null));
    EpicTask dummyEpic = findAddedTask(tm, EpicTask.class, idsBeforeDummyEpic);

    try {
      tm.addTask(
          new SubTaskCreationDTO(
              "Subtask Title Valid Boundary", "Tiny", dummyEpic.getId(), null, null));
    } catch (ValidationException e) {
      System.out.println(
          "Caught expected ValidationException for short description on SubTask: "
              + e.getMessage());
    } catch (TaskNotFoundException e) {
      System.out.println(
          "Caught unexpected TaskNotFoundException for SubTask boundary: " + e.getMessage());
    }

    try {
      tm.addTask(
          new SubTaskCreationDTO(
              "Subtask Title Valid Boundary",
              "Valid Subtask Description Boundary",
              UUID.randomUUID(),
              null,
              null));
    } catch (ValidationException e) {
      System.out.println(
          "Caught expected ValidationException for non-existent epicId on SubTask: "
              + e.getMessage());
    } catch (TaskNotFoundException e) {
      System.out.println(
          "Caught TaskNotFoundException for non-existent epicId on SubTask: " + e.getMessage());
    }

    try {
      tm.addTask(new EpicTaskCreationDTO("Epic short", "Desc short", null));
    } catch (ValidationException e) {
      System.out.println(
          "Caught expected ValidationException for short title/desc on Epic: " + e.getMessage());
    }

    System.out.println("Final state in testBoundaryCases:");
    printAll(tm);
    System.out.print(TEST_SECTION_END);
  }

  private static void printAll(TaskManager tm) {
    System.out.println("--- All tasks in the repository ---");
    Collection<Task> tasks = tm.getAllTasks();
    if (tasks.isEmpty()) {
      System.out.println("No tasks in repository.");
    } else {
      tasks.forEach(System.out::println);
    }
    System.out.println("-----------------------------------\n");
  }
}
