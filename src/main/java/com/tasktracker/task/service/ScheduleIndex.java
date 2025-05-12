package com.tasktracker.task.service;

import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.util.List;

public interface ScheduleIndex {
  void add(Task task) throws ValidationException;

  void update(Task oldTask, Task newTask) throws ValidationException;

  void updateEpicAndSubtask(
      SubTask oldSubtask, SubTask newSubtask, EpicTask oldEpicTask, EpicTask newEpicTask)
      throws ValidationException;

  void remove(Task task);

  boolean hasOverlap(Task task);

  List<Task> asOrderedList();
}
