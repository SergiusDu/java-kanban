package com.tasktracker.task.service;

import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.exception.ValidationException;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.util.List;

public interface ScheduleIndex {
  void add(Task task) throws ValidationException, OverlapException;

  void update(Task oldTask, Task newTask) throws ValidationException, OverlapException;

  void updateEpicAndSubtask(
      SubTask oldSubtask, SubTask newSubtask, EpicTask oldEpicTask, EpicTask newEpicTask)
      throws ValidationException, OverlapException;

  void remove(Task task);

  boolean hasOverlap(Task task);

  List<Task> asOrderedList();
}
