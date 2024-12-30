package com.tasktracker.task.dto;

public record SubTaskCreationDTO(String title, String description, int epicId)
    implements TaskCreationDTO {}
