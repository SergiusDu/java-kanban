package com.tasktracker.task.dto;

import com.tasktracker.annotation.NotNull;
import java.time.LocalDateTime;

/**
 * Data transfer object for creating new epic tasks. Epic tasks are parent tasks that can contain
 * subtasks.
 */
public record EpicTaskCreationDTO(
    /* The title of the epic task. Cannot be null. */
    @NotNull String title,
    /* The description of the epic task. Cannot be null. */
    @NotNull String description,
    /* The start time when the epic task is scheduled to begin. Optional. */
    LocalDateTime startTime)
    implements TaskCreationDTOs {}
