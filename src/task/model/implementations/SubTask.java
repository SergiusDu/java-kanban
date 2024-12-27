package task.model.implementations;

import task.model.enums.TaskStatus;

public final class SubTask extends Task {
    private final int epicTaskId;

    public SubTask(int id, String title, String description, TaskStatus status, int epicTaskId) {
        super(id, title, description, status);
    this.epicTaskId = epicTaskId;
    }

    public int getEpicTaskId() {
        return epicTaskId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + super.getId() +
                ", title='" + super.getTitle() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status=" + super.getStatus()  +
                ", epicTaskId=" + epicTaskId +
                '}';
    }
}
