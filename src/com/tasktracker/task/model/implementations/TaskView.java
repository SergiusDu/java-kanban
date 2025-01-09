package com.tasktracker.task.model.implementations;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a view of a specific task, containing details such as the view's unique ID, the
 * timestamp when the view was created, and the ID of the associated task.
 */
public final class TaskView implements Comparable<TaskView> {
  private final UUID viewId;
  private final int taskId;
  private final LocalDateTime viewDateTime;

  /**
   * Creates a new {@code TaskView} instance with a unique identifier and the current timestamp.
   *
   * @param taskId the ID of the task being viewed
   */
  public TaskView(int taskId, LocalDateTime viewDateTime) {
    this.viewId = UUID.randomUUID();
    this.viewDateTime = viewDateTime;
    this.taskId = taskId;
  }

  /**
   * Retrieves the unique identifier for this task view.
   *
   * @return the unique ID of the task view
   */
  public UUID getViewId() {
    return viewId;
  }

  /**
   * Retrieves the timestamp indicating when this task view was created.
   *
   * @return the creation time of the task view
   */
  public LocalDateTime getViewDateTime() {
    return viewDateTime;
  }

  /**
   * Retrieves the ID of the task associated with this view.
   *
   * @return the ID of the associated task
   */
  public int getTaskId() {
    return taskId;
  }

  /**
   * Compares the specified object with this {@code TaskView} for equality.
   *
   * @param object the object to be compared for equality with this task view
   * @return {@code true} if the specified object is equal to this task view; {@code false}
   *     otherwise
   */
  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    TaskView taskView = (TaskView) object;
    return taskId == taskView.taskId
        && Objects.equals(this.viewDateTime, taskView.getViewDateTime())
        && Objects.equals(this.viewId, taskView.getViewId());
  }

  /**
   * Returns the hash code value for this {@code TaskView}.
   *
   * @return the hash code value for this task view
   */
  @Override
  public int hashCode() {
    return Objects.hash(viewId, taskId, viewDateTime);
  }

  /**
   * Returns a string representation of this {@code TaskView}.
   *
   * @return a string representation of this task view
   */
  @Override
  public String toString() {
    return "TaskView{" + "viewDateTime=" + viewDateTime + ", taskId=" + taskId + '}';
  }

  /**
   * Compares this {@code TaskView} instance with another {@code TaskView} for ordering based on the
   * view creation timestamp, task ID, and unique view ID.
   *
   * @param other the {@code TaskView} to compare with this instance
   * @return a negative integer, zero, or a positive integer as this {@code TaskView}'s creation
   *     timestamp is earlier than, equal to, or later than the specified {@code TaskView}'s
   *     creation timestamp. If timestamps are equal, compares by task ID, and if task IDs are also
   *     equal, compares by view ID.
   */
  @Override
  public int compareTo(TaskView other) {
    int cmp = this.viewDateTime.compareTo(other.viewDateTime);
    if (cmp != 0) {
      return cmp;
    }
    cmp = Integer.compare(this.taskId, other.getTaskId());
    if (cmp != 0) {
      return cmp;
    }
    return this.viewId.compareTo(other.viewId);
  }
}
