package com.tasktracker.task.dto;

import com.tasktracker.annotation.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data transfer object for creating new subtasks. Subtasks are tasks that are part of an epic task
 * and inherit some properties from their parent epic.
 */
public record SubTaskCreationDTO(
    /* The title of the subtask. Cannot be null. */
    @NotNull String title,
    /* The description of the subtask. Cannot be null. */
    @NotNull String description,
    /* The ID of the parent epic task this subtask belongs to. Cannot be null. */
    @NotNull UUID epicId,
    /* The start time when the subtask is scheduled to begin. Optional. */
    LocalDateTime startTime,
    /* The planned duration of the subtask. Optional. */
    Duration duration)
    implements TaskCreationDTOs {}
