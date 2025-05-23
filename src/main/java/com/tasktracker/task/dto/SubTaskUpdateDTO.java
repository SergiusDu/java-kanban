package com.tasktracker.task.dto;

import com.tasktracker.annotation.NotNull;
import com.tasktracker.task.model.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data transfer object for updating existing subtasks. Contains all the fields necessary to modify
 * a subtask's properties.
 */
public record SubTaskUpdateDTO(
    /* The unique identifier of the subtask to update. Cannot be null. */
    @NotNull UUID id,
    /* The new title for the subtask. Cannot be null. */
    @NotNull String title,
    /* The new description for the subtask. Cannot be null. */
    @NotNull String description,
    /* The new status for the subtask. Cannot be null. */
    @NotNull TaskStatus status,
    /* The ID of the parent epic task this subtask belongs to. Cannot be null. */
    @NotNull UUID epicId,
    /* The new start time when the subtask is scheduled to begin. Optional. */
    LocalDateTime startTime,
    /* The new planned duration of the subtask. Optional. */
    Duration duration)
    implements TaskUpdateDTOs {}
