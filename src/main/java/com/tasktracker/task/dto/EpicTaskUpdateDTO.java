package com.tasktracker.task.dto;

import com.tasktracker.annotation.NotNull;
import java.util.UUID;

/**
 * Data transfer object for updating existing epic tasks. Epic tasks are parent tasks that can
 * contain subtasks.
 */
public record EpicTaskUpdateDTO(
    /* The unique identifier of the epic task to update. Cannot be null. */
    @NotNull UUID id,
    /* The new title for the epic task. Cannot be null. */
    @NotNull String title,
    /* The new description for the epic task. Cannot be null. */
    @NotNull String description)
    implements TaskUpdateDTOs {}
