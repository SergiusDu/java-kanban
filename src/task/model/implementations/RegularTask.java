package task.model.implementations;

import task.model.enums.TaskStatus;

public final class RegularTask extends Task {
    public RegularTask(int id, String title, String description, TaskStatus status) {
        super(id, title, description, status);
    }

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
