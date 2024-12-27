package task.dto;

import task.model.enums.TaskStatus;

public record SubTaskUpdateDTO(
    int id, String title, String description, TaskStatus status, int epicId) {}
