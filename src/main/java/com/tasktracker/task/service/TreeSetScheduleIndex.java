package com.tasktracker.task.service;

import com.tasktracker.task.exception.OverlapException;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public final class TreeSetScheduleIndex implements ScheduleIndex {

  private static final Comparator<Task> CMP =
      Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparing(Task::getEndTime, Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparing(Task::getId);

  private final NavigableSet<Task> timeLine = new TreeSet<>(CMP);

  private static boolean intersect(Task a, Task b) {
    if (a == null || b == null) {
      return false;
    }
    LocalDateTime startA = a.getStartTime();
    LocalDateTime endA = a.getEndTime();
    LocalDateTime startB = b.getStartTime();
    LocalDateTime endB = b.getEndTime();

    if (startA == null || endA == null || startB == null || endB == null) {
      return false;
    }
    return startA.isBefore(endB) && startB.isBefore(endA);
  }

  private boolean hasConflict(Task taskToCheck, Task existingTask) {
    if (taskToCheck == null || existingTask == null) {
      return false;
    }
    if (taskToCheck instanceof EpicTask) {
      return false;
    }
    if (taskToCheck.getId().equals(existingTask.getId())) {
      return false;
    }

    if (existingTask instanceof EpicTask existingEpic
        && taskToCheck instanceof SubTask sub
        && Objects.equals(sub.getEpicTaskId(), existingEpic.getId())) {
      return false;
    }

    return intersect(existingTask, taskToCheck);
  }

  private boolean checkOverlapAgainstCollection(Task task, Collection<Task> tasksToCompareAgainst) {
    if (task.getStartTime() == null || task.getEndTime() == null) {
      return false;
    }
    return tasksToCompareAgainst.stream()
        .filter(Objects::nonNull)
        .anyMatch(existingTask -> hasConflict(task, existingTask));
  }

  @Override
  public void add(Task task) throws OverlapException {
    Objects.requireNonNull(task, "Task to add cannot be null");
    ensureNoOverlap(task);
    timeLine.add(task);
  }

  @Override
  public void update(Task oldTask, Task newTask) throws OverlapException {
    Objects.requireNonNull(oldTask, "Old task cannot be null for update");
    Objects.requireNonNull(newTask, "New task cannot be null for update");

    NavigableSet<Task> tempTimeLine = new TreeSet<>(CMP);
    tempTimeLine.addAll(this.timeLine);
    tempTimeLine.remove(oldTask);

    if (checkOverlapAgainstCollection(newTask, tempTimeLine)) {
      throw new OverlapException(
          String.format(
              "Time overlap detected for updated task. Task ID %s with start time '%s' and end time '%s'"
                  + " overlaps with an existing task in schedule",
              newTask.getId(), newTask.getStartTime(), newTask.getEndTime()));
    }

    timeLine.remove(oldTask);
    timeLine.add(newTask);
  }

  @Override
  public void updateEpicAndSubtask(
      SubTask oldSubtask, SubTask newSubtask, EpicTask oldEpicTask, EpicTask newEpicTask)
      throws OverlapException {
    Objects.requireNonNull(oldSubtask, "Old subtask cannot be null");
    Objects.requireNonNull(newSubtask, "New subtask cannot be null");
    Objects.requireNonNull(oldEpicTask, "Old epic task cannot be null");
    Objects.requireNonNull(newEpicTask, "New epic task cannot be null");

    NavigableSet<Task> tempTimeLine = new TreeSet<>(CMP);
    tempTimeLine.addAll(this.timeLine);
    tempTimeLine.remove(oldSubtask);
    tempTimeLine.remove(oldEpicTask);

    if (checkOverlapAgainstCollection(newSubtask, tempTimeLine)) {
      throw new OverlapException(
          String.format(
              "Time overlap detected for new subtask. Task ID %s with start time '%s' and end time '%s' overlaps.",
              newSubtask.getId(), newSubtask.getStartTime(), newSubtask.getEndTime()));
    }

    tempTimeLine.add(newSubtask);

    if (checkOverlapAgainstCollection(newEpicTask, tempTimeLine)) {
      throw new OverlapException(
          String.format(
              "Time overlap detected for new epic task. Task ID %s with start time '%s' and end time '%s' overlaps.",
              newEpicTask.getId(), newEpicTask.getStartTime(), newEpicTask.getEndTime()));
    }

    timeLine.remove(oldSubtask);
    timeLine.add(newSubtask);
    timeLine.remove(oldEpicTask);
    timeLine.add(newEpicTask);
  }

  private void ensureNoOverlap(Task newTask) throws OverlapException {
    Objects.requireNonNull(newTask, "Task for overlap check cannot be null");
    if (hasOverlap(newTask)) {
      throw new OverlapException(
          String.format(
              "Time overlap detected. Task ID %s with start time '%s' and end time '%s'"
                  + " overlaps with an existing task in schedule",
              newTask.getId(), newTask.getStartTime(), newTask.getEndTime()));
    }
  }

  @Override
  public void remove(Task task) {
    Objects.requireNonNull(task, "Task to remove cannot be null");
    timeLine.remove(task);
  }

  @Override
  public boolean hasOverlap(Task task) {
    Objects.requireNonNull(task, "Task for overlap check cannot be null");
    if (task.getStartTime() == null || task.getEndTime() == null) {
      return false;
    }
    return this.timeLine.stream()
        .filter(Objects::nonNull)
        .anyMatch(existingTask -> hasConflict(task, existingTask));
  }

  @Override
  public List<Task> asOrderedList() {
    return List.copyOf(timeLine);
  }
}
