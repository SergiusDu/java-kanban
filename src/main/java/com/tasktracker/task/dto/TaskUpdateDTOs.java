package com.tasktracker.task.dto;

public sealed interface TaskUpdateDTOs
    permits RegularTaskUpdateDTO, SubTaskUpdateDTO, EpicTaskUpdateDTO {}
