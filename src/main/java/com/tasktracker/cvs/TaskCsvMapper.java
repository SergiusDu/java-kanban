package com.tasktracker.cvs;

import com.tasktracker.cvs.exceptions.CvsMapperException;
import com.tasktracker.cvs.util.CsvUtil;
import com.tasktracker.task.model.enums.TaskStatus;
import com.tasktracker.task.model.implementations.EpicTask;
import com.tasktracker.task.model.implementations.RegularTask;
import com.tasktracker.task.model.implementations.SubTask;
import com.tasktracker.task.model.implementations.Task;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
          + "\"startTime\","
          + "\"duration\","
          + "\"created\","
          + "\"updated\"";
  private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final String REGULAR_TASK_NAME = "REGULAR";
  private static final String EPIC_TASK_NAME = "EPIC";
  private static final String SUBTASK_TASK_NAME = "SUBTASK";

  private TaskCsvMapper() {}

  private static String joinIds(Set<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return "";
    }
    return ids.stream().map(UUID::toString).collect(Collectors.joining(";"));
  }

  private static String quote(String s) {
    if (s == null) {
      return "\"\"";
    }
    return "\"" + s.replace("\"", ESCAPED_QUOTE) + "\"";
  }

  private static String unquote(String s) {
    if (s == null)
      return ""; // Если сама строка null (например, из-за ошибки split), вернуть пустую строку
    if ("\"\"".equals(s))
      return ""; // Если строка это "" (пустая строка в кавычках), вернуть пустую строку
    if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1).replace(ESCAPED_QUOTE, "\"");
    }
    return s; // Возвращаем как есть, если не обрамлено кавычками (например, литерал "null")
  }

  public static Task fromCsv(String line) {
    if (line == null || line.isBlank()) {
      throw new CvsMapperException("CSV line is null or blank: [" + line + "]");
    }
    String[] parts = CsvUtil.smartSplit(line, DELIMITER.charAt(0)).toArray(new String[0]);
    final int EXPECTED_FIELDS = 11;
    if (parts.length < EXPECTED_FIELDS) {
      throw new CvsMapperException(
          "Incorrect number of fields in CSV line. Expected "
              + EXPECTED_FIELDS
              + ", got "
              + parts.length
              + ". Line: "
              + line);
    }

    int idx = 0;
    try {
      UUID id = UUID.fromString(unquote(parts[idx++]));
      String type = unquote(parts[idx++]);
      String title = unquote(parts[idx++]);
      String description = unquote(parts[idx++]);
      TaskStatus status = TaskStatus.valueOf(unquote(parts[idx++]));

      String epicIdRaw = unquote(parts[idx++]);
      UUID epicId =
          (epicIdRaw.isEmpty() || "null".equalsIgnoreCase(epicIdRaw))
              ? null
              : UUID.fromString(epicIdRaw);

      String subtasksRaw = unquote(parts[idx++]);
      Set<UUID> subtaskIds = CsvUtil.parseIds(subtasksRaw);

      String startTimeRaw = unquote(parts[idx++]);
      LocalDateTime startTime =
          (startTimeRaw.isEmpty() || "null".equalsIgnoreCase(startTimeRaw))
              ? null
              : LocalDateTime.parse(startTimeRaw);

      String durationRaw = unquote(parts[idx++]);
      Duration duration =
          (durationRaw.isEmpty() || "null".equalsIgnoreCase(durationRaw))
              ? null
              : Duration.parse(durationRaw);

      LocalDateTime createdAt = LocalDateTime.parse(unquote(parts[idx++]), FMT);
      LocalDateTime updatedAt = LocalDateTime.parse(unquote(parts[idx++]), FMT);

      return switch (type) {
        case REGULAR_TASK_NAME ->
            new RegularTask(
                id, title, description, status, createdAt, updatedAt, startTime, duration);
        case EPIC_TASK_NAME ->
            new EpicTask(
                id,
                title,
                description,
                status,
                subtaskIds,
                createdAt,
                updatedAt,
                startTime,
                duration);
        case SUBTASK_TASK_NAME -> {
          if (epicId == null)
            throw new CvsMapperException("SubTask must have an epicId. Line: " + line);
          yield new SubTask(
              id, title, description, status, epicId, createdAt, updatedAt, startTime, duration);
        }
        default -> throw new CvsMapperException("Unknown task type " + type + ". Line: " + line);
      };
    } catch (DateTimeParseException | IllegalArgumentException e) {
      throw new CvsMapperException(
          "Failed to parse CSV data at line: " + line + ". Error: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new CvsMapperException(
          "Unexpected error parsing CSV line: " + line + ". Error: " + e.getMessage(), e);
    }
  }

  public static String toCsv(Task task) {
    StringBuilder sb = new StringBuilder();
    sb.append(quote(task.getId().toString())).append(DELIMITER);
    sb.append(quote(taskType(task))).append(DELIMITER);
    sb.append(quote(task.getTitle())).append(DELIMITER);
    sb.append(quote(task.getDescription())).append(DELIMITER);
    sb.append(quote(task.getStatus().toString())).append(DELIMITER);

    if (task instanceof SubTask subTask) {
      sb.append(quote(subTask.getEpicTaskId().toString()));
    } else {
      sb.append(quote(""));
    }
    sb.append(DELIMITER);

    if (task instanceof EpicTask epicTask) {
      sb.append(quote(joinIds(epicTask.getSubtaskIds())));
    } else {
      sb.append(quote(""));
    }
    sb.append(DELIMITER);

    sb.append(task.getStartTime() != null ? quote(task.getStartTime().toString()) : quote(""))
        .append(DELIMITER);
    sb.append(task.getDuration() != null ? quote(task.getDuration().toString()) : quote(""))
        .append(DELIMITER);

    sb.append(quote(task.getCreationDate().format(FMT))).append(DELIMITER);
    sb.append(quote(task.getUpdateDate().format(FMT)));

    return sb.toString();
  }

  private static String taskType(Task task) {
    return switch (task) {
      case RegularTask ignored -> REGULAR_TASK_NAME;
      case SubTask ignored -> SUBTASK_TASK_NAME;
      case EpicTask ignored -> EPIC_TASK_NAME;
      default ->
          throw new CvsMapperException("Unknown Task subclass: " + task.getClass().getName());
    };
  }
}
