package com.tasktracker.task.dto;

public record EpicTaskCreationDTO(String title, String description) implements TaskCreationDTO {}
