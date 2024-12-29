package task.model.implementations;

import task.exception.ValidationException;
import task.model.enums.TaskStatus;

public final class SubTask extends Task {
  private final int epicTaskId;

  public SubTask(int id, String title, String description, TaskStatus status, int epicTaskId) {
    super(id, title, description, status);
    this.epicTaskId = getValidEpicTaskId(epicTaskId);
  }

  public int getEpicTaskId() {
    return epicTaskId;
  }

  private int getValidEpicTaskId(int epicTaskId) {
    if (epicTaskId < 0) {
      throw new ValidationException("Epic Task ID must be a non-negative integer.");
    }
    return epicTaskId;
  }

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
