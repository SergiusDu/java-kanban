package com.tasktracker.task.model.implementations;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskViewTest {

  private static final UUID TEST_UUID_1 = UUID.randomUUID();
  private static final UUID TEST_UUID_2 = UUID.randomUUID();
  private static final UUID TEST_UUID_3 = UUID.randomUUID();

  @Test
  @DisplayName("Constructor should create TaskView with valid parameters")
  void constructor_ValidParameters() {
    LocalDateTime viewTime = LocalDateTime.now();
    TaskView taskView = new TaskView(TEST_UUID_1, viewTime);
    assertEquals(TEST_UUID_1, taskView.getTaskId());
    assertEquals(viewTime, taskView.getViewDateTime());
  }

  @Test
  @DisplayName("getViewDateTime should return the correct viewDateTime")
  void getViewDateTime_ReturnsCorrectValue() {
    LocalDateTime viewTime = LocalDateTime.of(2023, 3, 1, 10, 0);
    TaskView taskView = new TaskView(TEST_UUID_2, viewTime);
    assertEquals(viewTime, taskView.getViewDateTime());
  }

  @Test
  @DisplayName("getTaskId should return the correct taskId")
  void getTaskId_ReturnsCorrectValue() {
    LocalDateTime viewTime = LocalDateTime.now();
    TaskView taskView = new TaskView(TEST_UUID_3, viewTime);
    assertEquals(TEST_UUID_3, taskView.getTaskId());
  }

  @Test
  @DisplayName("equals should return true for the same TaskView instance")
  void equals_SameInstance_ReturnsTrue() {
    TaskView taskView = new TaskView(TEST_UUID_1, LocalDateTime.now());
    assertEquals(taskView, taskView);
  }

  @Test
  @DisplayName("equals should return true for different instances with the same taskId")
  void equals_DifferentInstancesSameTaskId_ReturnsTrue() {
    LocalDateTime viewTime1 = LocalDateTime.now();
    LocalDateTime viewTime2 = LocalDateTime.now().plusHours(1); // Different view time
    TaskView taskView1 = new TaskView(TEST_UUID_1, viewTime1);
    TaskView taskView2 = new TaskView(TEST_UUID_1, viewTime2); // Same taskId
    assertEquals(taskView1, taskView2, "TaskViews with the same taskId should be equal.");
  }

  @Test
  @DisplayName("equals should return false for different TaskView instances with different taskIds")
  void equals_DifferentInstancesDifferentTaskId_ReturnsFalse() {
    TaskView taskView1 = new TaskView(TEST_UUID_1, LocalDateTime.now());
    TaskView taskView2 = new TaskView(TEST_UUID_2, LocalDateTime.now()); // Different taskId
    assertNotEquals(taskView1, taskView2);
  }

  @Test
  @DisplayName("equals should return false when comparing with null")
  void equals_CompareWithNull_ReturnsFalse() {
    TaskView taskView1 = new TaskView(TEST_UUID_1, LocalDateTime.now());
    assertNotEquals(null, taskView1);
  }

  @Test
  @DisplayName("equals should return false when comparing with different object type")
  void equals_CompareWithDifferentType_ReturnsFalse() {
    TaskView taskView1 = new TaskView(TEST_UUID_1, LocalDateTime.now());
    assertNotEquals("A String", taskView1);
  }

  @Test
  @DisplayName("Constructor should not allow both TaskViews with null taskId")
  void equals_NullTaskIdInBothObjects_ThrowsException() {
    LocalDateTime now = LocalDateTime.now();
    assertThrows(
        NullPointerException.class,
        () -> new TaskView(null, now),
        "Constructor should not allow null taskId");
    assertThrows(
        NullPointerException.class,
        () -> new TaskView(null, now.plusSeconds(1)),
        "Constructor should not allow null taskId");
  }

  @Test
  @DisplayName("hashCode should be consistent for the same TaskView instance")
  void hashCode_SameInstance_IsConsistent() {
    TaskView taskView = new TaskView(TEST_UUID_1, LocalDateTime.now());
    assertEquals(taskView.hashCode(), taskView.hashCode());
  }

  @Test
  @DisplayName("hashCode should be same for different instances with the same taskId")
  void hashCode_DifferentInstancesSameTaskId_IsSame() {
    LocalDateTime viewTime1 = LocalDateTime.now();
    LocalDateTime viewTime2 = LocalDateTime.now().plusHours(1);
    TaskView taskView1 = new TaskView(TEST_UUID_1, viewTime1);
    TaskView taskView2 = new TaskView(TEST_UUID_1, viewTime2); // Same taskId
    assertEquals(taskView1.hashCode(), taskView2.hashCode());
  }

  @Test
  @DisplayName("hashCode should differ for different TaskView instances with different taskIds")
  void hashCode_DifferentInstancesDifferentTaskIds_Differs() {
    TaskView taskView1 = new TaskView(TEST_UUID_1, LocalDateTime.now());
    TaskView taskView2 = new TaskView(TEST_UUID_2, LocalDateTime.now()); // Different taskId
    assertNotEquals(taskView1.hashCode(), taskView2.hashCode());
  }

  @Test
  @DisplayName("compareTo should order based on taskId")
  void compareTo_OrderBasedOnTaskId() {
    // Create UUIDs that have a known sort order for testing
    UUID idSmall = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID idMedium = UUID.fromString("00000000-0000-0000-0000-000000000002");
    UUID idLarge = UUID.fromString("00000000-0000-0000-0000-000000000003");

    LocalDateTime now = LocalDateTime.now();
    TaskView taskViewSmall = new TaskView(idSmall, now);
    TaskView taskViewMedium = new TaskView(idMedium, now.plusHours(1)); // Different viewTime
    TaskView taskViewLarge = new TaskView(idLarge, now.minusHours(1)); // Different viewTime

    assertTrue(taskViewSmall.compareTo(taskViewMedium) < 0);
    assertTrue(taskViewMedium.compareTo(taskViewLarge) < 0); // Based on UUID string comparison
    assertTrue(taskViewSmall.compareTo(taskViewLarge) < 0);

    assertTrue(taskViewMedium.compareTo(taskViewSmall) > 0);
    assertTrue(taskViewLarge.compareTo(taskViewMedium) > 0);
    assertTrue(taskViewLarge.compareTo(taskViewSmall) > 0);

    assertEquals(0, taskViewSmall.compareTo(new TaskView(idSmall, now.plusDays(1))));
  }

  @Test
  @DisplayName("compareTo should handle equal TaskViews correctly (same taskId)")
  void compareTo_EqualTaskViews_ReturnsZero() {
    LocalDateTime viewTime = LocalDateTime.of(2023, 6, 1, 9, 0);
    TaskView taskView1 = new TaskView(TEST_UUID_1, viewTime);
    TaskView taskView2 = new TaskView(TEST_UUID_1, viewTime.plusMinutes(30)); // Same taskId
    assertEquals(0, taskView1.compareTo(taskView2));
  }

  @Test
  @DisplayName("compareTo should throw NullPointerException if other is null")
  void compareTo_NullOther_ThrowsNullPointerException() {
    TaskView taskView1 = new TaskView(TEST_UUID_1, LocalDateTime.now());
    assertThrows(NullPointerException.class, () -> taskView1.compareTo(null));
  }

  @Test
  @DisplayName("toString should return a non-empty string representation")
  void toString_ReturnsNonEmptyString() {
    TaskView taskView = new TaskView(TEST_UUID_1, LocalDateTime.now());
    String str = taskView.toString();
    assertNotNull(str);
    assertFalse(str.isEmpty());
    assertTrue(str.contains(TEST_UUID_1.toString()));
    assertTrue(str.contains("viewDateTime="));
  }
}
