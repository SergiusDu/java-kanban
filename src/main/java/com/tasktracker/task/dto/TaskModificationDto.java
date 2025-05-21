package com.tasktracker.task.dto;

public sealed interface TaskModificationDto
    permits RegularTaskCreationDTO,
        RegularTaskUpdateDTO,
        SubTaskCreationDTO,
        SubTaskUpdateDTO,
        EpicTaskCreationDTO,
        EpicTaskUpdateDTO {}
