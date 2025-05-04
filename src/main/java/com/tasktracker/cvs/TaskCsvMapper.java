package com.tasktracker.cvs;

import com.tasktracker.cvs.exceptions.CvsMapperException;
import com.tasktracker.cvs.util.CsvUtil;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for mapping tasks to and from CSV format. Handles serialization and deserialization
 * of different task types.
 */
public class TaskCsvMapper {
  public static final String DELIMITER = ",";
  public static final String ESCAPED_QUOTE = "\"\"";
  public static final String CSV_HEADER =
      "\"id\","
          + "\"type\","
          + "\"title\","
          + "\"description\","
          + "\"status\","
          + "\"epicId\","
          + "\"subtasks\","
          + "\"created\","
          + "\"updated\"";
  private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final String REGULAR_TASK_NAME = "REGULAR";
  private static final String EPIC_TASK_NAME = "EPIC";
  private static final String SUBTASK_TASK_NAME = "SUBTASK";

  private TaskCsvMapper() {}

  /**
   * Converts a CSV line into a Task object.
   *
   * @param line CSV line containing task data
   * @return Task object (RegularTask, EpicTask, or SubTask) parsed from the CSV line
   * @throws CvsMapperException if parsing fails or task type is unknown
   */
  public static Task fromCvs(String line) {
    String[] parts = CsvUtil.smartSplit(line, DELIMITER.charAt(0)).toArray(new String[0]);
    int idx = 0;
    try {
      int id = Integer.parseInt(parts[idx++]);
      String type = unquote(parts[idx++]);
      String title = unquote(parts[idx++]);
      String description = unquote(parts[idx++]);
      TaskStatus status = TaskStatus.valueOf(unquote(parts[idx++]));
      int tempIdx = idx++;
      int epicId = parts[tempIdx].isEmpty() ? -1 : Integer.parseInt(parts[tempIdx]);
      String subtasksRaw = parts[idx++];
      LocalDateTime createdAt = LocalDateTime.parse(parts[idx++], FMT);
      LocalDateTime updatedAt = LocalDateTime.parse(parts[idx], FMT);
      return switch (type) {
        case REGULAR_TASK_NAME ->
            new RegularTask(id, title, description, status, createdAt, updatedAt);
        case EPIC_TASK_NAME ->
            new EpicTask(
                id,
                title,
                description,
                status,
                CsvUtil.parseIds(subtasksRaw),
                createdAt,
                updatedAt);
        case SUBTASK_TASK_NAME ->
            new SubTask(id, title, description, status, epicId, createdAt, updatedAt);
        default -> throw new CvsMapperException("Unknown task type " + type);
      };
    } catch (Exception e) {
      throw new CvsMapperException("Failed to parse CVS at line: " + line, e);
    }
  }

  /**
   * Converts a Task object into a CSV line.
   *
   * @param task Task object to convert
   * @return CSV formatted string representing the task
   */
  public static String toCvs(Task task) {
    StringBuilder sb = new StringBuilder();
    sb.append(task.getId()).append(DELIMITER);
    sb.append(quote(taskType(task))).append(DELIMITER);
    sb.append(quote(task.getTitle())).append(DELIMITER);
    sb.append(quote(task.getDescription())).append(DELIMITER);
    sb.append(quote(task.getStatus().toString())).append(DELIMITER);
    if (task instanceof SubTask subTask) {
      sb.append(subTask.getEpicTaskId());
    }
    sb.append(DELIMITER);
    if (task instanceof EpicTask epicTask) {
      sb.append(join(epicTask.getSubtaskIds()));
    }
    sb.append(DELIMITER);

    sb.append(task.getCreationDate().format(FMT));
    sb.append(DELIMITER);
    sb.append(task.getUpdateDate().format(FMT));
    return sb.toString();
  }

  /**
   * Determines the type name of a task for CSV serialization.
   *
   * @param task Task object to get type for
   * @return String representing the task type
   * @throws CvsMapperException if task is of unknown type
   */
  private static String taskType(Task task) {
    return switch (task) {
      case RegularTask ignored -> REGULAR_TASK_NAME;
      case SubTask ignored -> SUBTASK_TASK_NAME;
      case EpicTask ignored -> EPIC_TASK_NAME;
      default -> throw new CvsMapperException("Unknown Task subclass");
    };
  }

  /**
   * Joins a set of integer IDs into a semicolon-delimited string.
   *
   * @param ids Set of integer IDs to join
   * @return Empty string if set is empty, otherwise semicolon-delimited string of IDs
   */
  private static String join(Set<Integer> ids) {
    return ids.isEmpty() ? "" : ids.stream().map(Object::toString).collect(Collectors.joining(";"));
  }

  /**
   * Wraps a string in quotes and escapes any quotes within.
   *
   * @param s String to quote
   * @return Quoted and escaped string
   */
  private static String quote(String s) {
    return '"' + s.replace("\"", ESCAPED_QUOTE) + '"';
  }

  /**
   * Removes surrounding quotes and unescapes any escaped quotes within.
   *
   * @param s Quoted string to unquote
   * @return Unquoted and unescaped string
   */
  private static String unquote(String s) {
    return s.substring(1, s.length() - 1).replace(ESCAPED_QUOTE, "\"");
  }
}
