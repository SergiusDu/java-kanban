package com.tasktracker.task.dto;

import java.util.UUID;

public record EpicTaskUpdateDTO(UUID id, String title, String description)
    implements TaskModificationDto {}
