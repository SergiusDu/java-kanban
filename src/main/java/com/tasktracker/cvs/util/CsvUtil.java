package com.tasktracker.cvs.util;

import com.tasktracker.cvs.exceptions.CsvParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CsvUtil {

  private CsvUtil() {}

  /**
   * Splits a single CSV record into fields, respecting double quotes and RFC 4180 escaping rules
   * («""» → «"» inside quoted field).
   *
   * @param line a single CSV record (no CR/LF)
   * @param delimiter usually ',' but kept parametric
   * @return list of raw (still quoted) field values, size ≥ 1
   * @throws CsvParseException if CSV is malformed (e.g., unmatched quotes) or null
   */
  public static List<String> smartSplit(String line, char delimiter) {
    if (line == null)
      throw new CsvParseException("CSV line must not be null", new IllegalArgumentException());

    List<String> result = new ArrayList<>(16);
    StringBuilder current = new StringBuilder(64);

    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);

      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
          current.append(c);
        }
      } else if (c == delimiter && !inQuotes) {
        result.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }

    if (inQuotes)
      throw new CsvParseException(
          "Unmatched quote in line: " + line, new IllegalArgumentException());

    result.add(current.toString());
    return result;
  }

  /**
   * Transforms a semicolon-delimited list of UUIDs into an immutable {@code Set<UUID>}.
   *
   * <p>Rules:
   *
   * <ul>
   *   <li>Empty or {@code null} input yields {@code Set.of()}.
   *   <li>Whitespace around UUIDs is ignored.
   *   <li>Duplicate values are removed.
   *   <li>Any invalid UUID token triggers {@link CsvParseException}.
   * </ul>
   *
   * @param raw semicolon-separated string of UUIDs, e.g. {@code
   *     "123e4567-e89b-12d3-a456-426614174000; 87e8a55d-31c8-4f5b-9e45-a6a234d12345"}
   * @return immutable set containing the parsed UUIDs
   * @throws CsvParseException if any token cannot be parsed as a valid UUID
   */
  public static Set<UUID> parseIds(String raw) {
    if (raw == null || raw.isBlank()) {
      return Set.of();
    }
    try {
      return Arrays.stream(raw.split(";"))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .map(UUID::fromString)
          .collect(Collectors.toUnmodifiableSet());
    } catch (NumberFormatException e) {
      throw new CsvParseException("Failed to parse UUID value", e);
    }
  }

  public static String joinUuidSet(Set<UUID> uuids) {
    return uuids.stream().map(UUID::toString).collect(Collectors.joining(";"));
  }
}
