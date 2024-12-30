package com.tasktracker.task.model.implementations;

import com.tasktracker.task.model.enums.TaskStatus;

/**
 * Represents a regular com.tasktracker.task with a unique ID, title, description, and status. It
 * extends the {@link Task} class to provide specific functionality for regular tasks.
 */
public final class RegularTask extends Task {

  /**
   * Constructs a new RegularTask with the specified ID, title, description, and status.
   *
   * @param id the unique identifier of the com.tasktracker.task
   * @param title the title of the com.tasktracker.task
   * @param description a brief description of the com.tasktracker.task
   * @param status the current status of the com.tasktracker.task, as defined in {@link TaskStatus}
   */
  public RegularTask(
      final int id, final String title, final String description, final TaskStatus status) {
    super(id, title, description, status);
  }

  /**
   * Returns a string representation of this RegularTask, including its ID, title, description, and
   * status.
   *
   * @return a string representation of the com.tasktracker.task
   */
  @Override
  public String toString() {
    return "RegularTask{"
        + "id="
        + super.getId()
        + ", title='"
        + super.getTitle()
        + '\''
        + ", description='"
        + super.getDescription()
        + '\''
        + ", status="
        + super.getStatus()
        + '}';
  }
}
