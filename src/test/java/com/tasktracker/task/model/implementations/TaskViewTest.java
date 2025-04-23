package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskViewTest {

  @Test
  @DisplayName("Constructor should create TaskView with valid parameters")
  void constructor_ValidParameters() {
    int taskId = 1;
    LocalDateTime viewTime = LocalDateTime.now();
    TaskView taskView = new TaskView(taskId, viewTime);
    assertEquals(taskId, taskView.getTaskId());
    assertEquals(viewTime, taskView.getViewDateTime());
  }


  @Test
  @DisplayName("getViewDateTime should return the correct viewDateTime")
  void getViewDateTime() {
    int taskId = 3;
    LocalDateTime viewTime = LocalDateTime.of(2023, 3, 1, 10, 0);
    TaskView taskView = new TaskView(taskId, viewTime);

    assertEquals(viewTime, taskView.getViewDateTime());
  }

  @Test
  @DisplayName("getTaskId should return the correct taskId")
  void getTaskId() {
    int taskId = 4;
    LocalDateTime viewTime = LocalDateTime.now();
    TaskView taskView = new TaskView(taskId, viewTime);

    assertEquals(taskId, taskView.getTaskId());
  }

  @Test
  @DisplayName("equals should return true for the same TaskView instance")
  void equals_SameInstance() {
    TaskView taskView = new TaskView(5, LocalDateTime.now());
    assertEquals(taskView, taskView, "TaskView should equal itself");
  }

  @Test
  @DisplayName("equals should return false for different TaskView instances")
  void equals_DifferentInstances() {
    TaskView taskView1 = new TaskView(6, LocalDateTime.now());
    TaskView taskView2 = new TaskView(7, LocalDateTime.now());
    assertNotEquals(taskView1, taskView2, "Different TaskView instances should not be equal");
  }

  @Test
  @DisplayName("hashCode should be consistent for the same TaskView instance")
  void hashCode_SameInstance() {
    TaskView taskView = new TaskView(7, LocalDateTime.now());
    assertEquals(
        taskView.hashCode(),
        taskView.hashCode(),
        "hashCode should be consistent for the same instance");
  }

  @Test
  @DisplayName("hashCode should differ for different TaskView instances")
  void hashCode_DifferentInstances() {
    TaskView taskView1 = new TaskView(8, LocalDateTime.now());
    TaskView taskView2 = new TaskView(9, LocalDateTime.now());
    assertNotEquals(
        taskView1.hashCode(),
        taskView2.hashCode(),
        "Different TaskView instances should have different hashCodes");
  }


  @Test
  @DisplayName("compareTo should handle equal TaskViews correctly")
  void compareTo_EqualTaskViews() {
    LocalDateTime viewTime = LocalDateTime.of(2023, 6, 1, 9, 0);
    TaskView taskView1 = new TaskView(11, viewTime);
    TaskView taskView2 = new TaskView(12, viewTime);

    assertNotEquals(
        taskView1.getTaskId(),
        taskView2.getTaskId(),
        "Different TaskViews should have different TaskIDs");
    assertNotEquals(
        0, taskView1.compareTo(taskView2), "Different TaskViews should not be equal in compareTo");
  }
}
