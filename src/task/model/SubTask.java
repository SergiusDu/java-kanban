package task.model;

public final class SubTask extends Task {
    private final int epicTaskId;

    public SubTask(int id, String title, String description, TaskStatus status, int epicTaskId) {
        super(id, title, description, status);
        this.epicTaskId = getValidatedEpicTaskId(epicTaskId);
    }

    private int getValidatedEpicTaskId(int epicTaskId) {
        if(epicTaskId < 0) {
            throw new IllegalArgumentException("Epic task ID should be positive");
        }
        return epicTaskId;
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
