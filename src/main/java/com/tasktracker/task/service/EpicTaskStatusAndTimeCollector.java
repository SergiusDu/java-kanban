package com.tasktracker.task.service;

import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.SubTask;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collector;

public final class EpicTaskStatusAndTimeCollector {
  private EpicTaskStatusAndTimeCollector() {}

  public static Collector<SubTask, ?, EpicTaskAggregatedResult>
      aggregateEpicSubTaskPropertiesCollector() {
    return Collector.of(
        SubtaskAggregatedState::new,
        (aggregatedState, subtask) -> {
          updateSubtaskStatus(aggregatedState, subtask);
          updateTimeConstraints(aggregatedState, subtask);
        },
        (firstState, secondState) -> {
          SubtaskAggregatedState mergedState = new SubtaskAggregatedState();
          mergeStatuses(firstState, secondState, mergedState);
          mergeTimeConstraints(firstState, secondState, mergedState);
          return mergedState;
        },
        aggregatedState -> {
          TaskStatus status;
          if (!aggregatedState.hasInProgressTasks && !aggregatedState.hasCompletedTasks) {
            status = TaskStatus.NEW;
          } else if (aggregatedState.hasCompletedTasks
              && !aggregatedState.hasNewTasks
              && !aggregatedState.hasInProgressTasks) {
            status = TaskStatus.DONE;
          } else {
            status = TaskStatus.IN_PROGRESS;
          }

          Duration duration = null;
          if (aggregatedState.startDateTime != null && aggregatedState.endDateTime != null) {
            duration = Duration.between(aggregatedState.startDateTime, aggregatedState.endDateTime);
          }

          return new EpicTaskAggregatedResult(status, aggregatedState.startDateTime, duration);
        });
  }

  private static void mergeTimeConstraints(
      SubtaskAggregatedState firstState,
      SubtaskAggregatedState secondState,
      SubtaskAggregatedState mergedState) {
    if (firstState.startDateTime == null && secondState.startDateTime == null) {
      return;
    }
    if (firstState.startDateTime == null) {
      mergedState.startDateTime = secondState.startDateTime;
      mergedState.endDateTime = secondState.endDateTime;
      return;
    }
    if (secondState.startDateTime == null) {
      mergedState.startDateTime = firstState.startDateTime;
      mergedState.endDateTime = firstState.endDateTime;
      return;
    }
    mergedState.startDateTime =
        secondState.startDateTime.isBefore(firstState.startDateTime)
            ? secondState.startDateTime
            : firstState.startDateTime;
    mergedState.endDateTime =
        secondState.endDateTime.isAfter(firstState.endDateTime)
            ? secondState.endDateTime
            : firstState.endDateTime;
  }

  private static void mergeStatuses(
      SubtaskAggregatedState firstState,
      SubtaskAggregatedState secondState,
      SubtaskAggregatedState mergedState) {
    mergedState.hasNewTasks = firstState.hasNewTasks || secondState.hasNewTasks;
    mergedState.hasInProgressTasks =
        firstState.hasInProgressTasks || secondState.hasInProgressTasks;
    mergedState.hasCompletedTasks = firstState.hasCompletedTasks || secondState.hasCompletedTasks;
  }

  private static void updateTimeConstraints(SubtaskAggregatedState state, SubTask subtask) {
    var startTime = subtask.getStartTime();
    var endTime = subtask.getEndTime();
    if (startTime == null || endTime == null) return;
    if (state.startDateTime == null || startTime.isBefore(state.startDateTime)) {
      state.startDateTime = startTime;
    }
    if (state.endDateTime == null || endTime.isAfter(state.endDateTime)) {
      state.endDateTime = endTime;
    }
  }

  private static void updateSubtaskStatus(SubtaskAggregatedState state, SubTask subtask) {
    switch (subtask.getStatus()) {
      case NEW -> state.hasNewTasks = true;
      case IN_PROGRESS -> state.hasInProgressTasks = true;
      case DONE -> state.hasCompletedTasks = true;
    }
  }

  static class SubtaskAggregatedState {
    boolean hasNewTasks;
    boolean hasInProgressTasks;
    boolean hasCompletedTasks;
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
  }
}
