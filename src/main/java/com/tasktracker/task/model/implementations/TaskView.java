package com.tasktracker.task.model.implementations;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a view of a specific task, containing details such as the ID of the associated task
 * and the timestamp when the view was created.
 */
public final class TaskView implements Comparable<TaskView> {
  private final UUID taskId;
  private final LocalDateTime viewDateTime;

  /**
   * Creates a new {@code TaskView} instance with the specified task ID and timestamp.
   *
   * @param taskId the ID of the task being viewed
   * @param viewDateTime the timestamp when the task view was created
   */
  public TaskView(UUID taskId, LocalDateTime viewDateTime) {
    this.viewDateTime = Objects.requireNonNull(viewDateTime, "ViewDateTime can't be null");
    this.taskId = Objects.requireNonNull(taskId, "Task Id can't be null");
  }

  /**
   * Retrieves the timestamp indicating when this task view was created.
   *
   * @return the timestamp when this task view was created
   */
  public LocalDateTime getViewDateTime() {
    return viewDateTime;
  }

  /**
   * Retrieves the ID of the task associated with this view.
   *
   * @return the ID of the associated task
   */
  public UUID getTaskId() {
    return taskId;
  }

  /**
   * Compares the specified object with this {@code TaskView} for equality.
   *
   * @param object the object to be compared for equality with this {@code TaskView}
   * @return {@code true} if the specified object is a {@code TaskView} with the same task ID as
   *     this {@code TaskView}; {@code false} otherwise
   */
  @Override
  public boolean equals(Object object) {
    if (object == null || getClass() != object.getClass()) return false;
    TaskView taskView = (TaskView) object;
    return taskId.equals(taskView.taskId);
  }

  /**
   * Returns the hash code value for this {@code TaskView}. The hash code is computed based on the
   * task ID.
   *
   * @return the hash code value for this {@code TaskView}
   */
  @Override
  public int hashCode() {
    return Objects.hash(taskId);
  }

  /**
   * Returns a string representation of this {@code TaskView}.
   *
   * @return a string representation of this {@code TaskView} in the format:
   *     TaskView{taskId=<taskId>, viewDateTime=<viewDateTime>}
   */
  @Override
  public String toString() {
    return "TaskView{" + "taskId=" + taskId + ", viewDateTime=" + viewDateTime + '}';
  }

  /**
   * Compares this {@code TaskView} instance with another {@code TaskView} to determine their
   * relative order. The comparison is based on the task ID.
   *
   * @param other the {@code TaskView} to compare with this instance
   * @return a negative integer, zero, or a positive integer as this {@code TaskView}'s task ID is
   *     less than, equal to, or greater than the specified {@code TaskView}'s task ID
   */
  @Override
  public int compareTo(TaskView other) {
    return this.taskId.compareTo(other.getTaskId());
  }
}
