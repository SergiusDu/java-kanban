package com.tasktracker.task.model.implementations;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents an overarching com.tasktracker.task model that can contain subtasks. Extends the
 * {@link Task} class to include additional functionality for managing subtasks.
 */
public final class EpicTask extends Task {
  /** A set of IDs representing the subtasks associated with this epic com.tasktracker.task. */
  private final Set<Integer> subtaskIds;

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
   * @throws ValidationException if any validation criteria are not met
   * @throws NullPointerException if any parameter marked as non-null is null
   */
  public EpicTask(
      final int id,
      final String title,
      final String description,
      final TaskStatus status,
      final Set<Integer> subtaskIds,
      final LocalDateTime creationDateTime,
      final LocalDateTime updateDateTime)
      throws ValidationException, NullPointerException {
    super(id, title, description, status, creationDateTime, updateDateTime);
    this.subtaskIds = getValidatedSubTaskIds(subtaskIds);
  }

  /**
   * Retrieves an unmodifiable copy of the set of subtask IDs associated with this
   * com.tasktracker.task.
   *
   * @return a read-only view of the subtask IDs
   */
  public Set<Integer> getSubtaskIds() {
    return Set.copyOf(subtaskIds);
  }

  /**
   * Validates the provided set of subtask IDs to ensure they do not contain negative values.
   *
   * @param subtaskIds the set of subtask IDs to validate
   * @return a validated, unmodifiable copy of the subtask IDs
   * @throws ValidationException if any of the subtask IDs are negative
   */
  private Set<Integer> getValidatedSubTaskIds(final Set<Integer> subtaskIds) {
    if (subtaskIds.isEmpty()) return Set.of();
    boolean isValidated = subtaskIds.stream().anyMatch(subtaskId -> subtaskId < 0);
    if (isValidated) {
      throw new ValidationException("Subtask IDs cannot contain negative values.");
    }
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
