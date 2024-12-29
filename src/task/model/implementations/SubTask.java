package task.model.implementations;

import task.exception.ValidationException;
import task.model.enums.TaskStatus;

/**
 * Represents a subtask that belongs to a specific epic task in a task management system. Extends
 * the generic {@link Task} class and adds the association with an epic task.
 */
public final class SubTask extends Task {
  private final int epicTaskId;

  /**
   * Creates a new SubTask instance.
   *
   * @param id the unique identifier of the subtask
   * @param title the title of the subtask
   * @param description the description of the subtask
   * @param status the current status of the subtask
   * @param epicTaskId the ID of the epic task to which this subtask belongs
   * @throws ValidationException if the provided epic task ID is invalid
   */
  public SubTask(
      final int id,
      final String title,
      final String description,
      final TaskStatus status,
      final int epicTaskId) {
    super(id, title, description, status);
    this.epicTaskId = getValidEpicTaskId(epicTaskId);
  }

  /**
   * Gets the ID of the epic task to which this subtask belongs.
   *
   * @return the ID of the associated epic task
   */
  public int getEpicTaskId() {
    return epicTaskId;
  }

  /**
   * Validates the provided epic task ID.
   *
   * @param epicTaskId the ID of the epic task to validate
   * @return the validated epic task ID
   * @throws ValidationException if the epic task ID is negative
   */
  private int getValidEpicTaskId(int epicTaskId) {
    if (epicTaskId < 0) {
      throw new ValidationException("Epic Task ID must be a non-negative integer.");
    }
    return epicTaskId;
  }

  /**
   * Returns a string representation of the SubTask object.
   *
   * @return a string containing the subtask details in a readable format
   */
  @Override
  public String toString() {
    return "SubTask{"
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
        + ", epicTaskId="
        + epicTaskId
        + '}';
  }
}
