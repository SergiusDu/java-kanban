package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an overarching com.tasktracker.task model that can contain subtasks. Extends the
 * {@link Task} class to include additional functionality for managing subtasks.
 */
public final class EpicTask extends Task {
  /** A set of IDs representing the subtasks associated with this epic com.tasktracker.task. */
  private final Set<UUID> subtaskIds;

  /**
   * Constructs a new {@code EpicTask} instance with the specified parameters.
   *
   * @param id the unique identifier of the task; must be greater than 0
   * @param title the title of the task; cannot be null or shorter than the minimum required length
   * @param description a description of the task; cannot be null or shorter than the minimum
   *     required length
   * @param status the current status of the task; cannot be null
   * @param subtaskIds a set of IDs representing the subtasks associated with this epic task; cannot
   *     contain negative values
   * @param creationDateTime the creation date of the task; cannot be null
   * @param updateDateTime the last update time of the task; cannot be null
   * @param startTime the scheduled start time for the task; can be null
   * @param duration the planned duration of the task; can be null
   * @throws ValidationException if any validation criteria are not met
   */
  public EpicTask(
      final UUID id,
      final String title,
      final String description,
      final TaskStatus status,
      final Set<UUID> subtaskIds,
      final LocalDateTime creationDateTime,
      final LocalDateTime updateDateTime,
      final LocalDateTime startTime,
      final Duration duration)
      throws ValidationException {
    super(id, title, description, status, creationDateTime, updateDateTime, startTime, duration);
    this.subtaskIds = Set.copyOf(subtaskIds);
  }

  /**
   * Retrieves an unmodifiable copy of the set of subtask IDs associated with this
   * com.tasktracker.task.
   *
   * @return a read-only view of the subtask IDs
   */
  public Set<UUID> getSubtaskIds() {
    return Set.copyOf(subtaskIds);
  }

  /**
   * Returns a string representation of the epic com.tasktracker.task, including its ID, title,
   * description, status, creation time, update time, and associated subtask IDs.
   *
   * @return a string representation of this instance
   */
  @Override
  public String toString() {
    return "EpicTask{"
        + "id="
        + super.getId()
        + ", title='"
        + super.getTitle()
        + ", description='"
        + super.getDescription()
        + ", status="
        + super.getStatus()
        + ", subtaskIds="
        + subtaskIds.toString()
        + ", creationDate="
        + super.getCreationDate()
        + ", updateDate="
        + super.getUpdateDate()
        + '}';
  }
}
