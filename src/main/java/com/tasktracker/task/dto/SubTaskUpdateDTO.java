package com.tasktracker.task.dto;

import com.tasktracker.task.model.enums.TaskStatus;

public record SubTaskUpdateDTO(
    int id, String title, String description, TaskStatus status, int epicId) {}
