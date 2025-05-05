package com.tasktracker.cvs.exceptions;

public class CsvParseException extends RuntimeException {
  public CsvParseException(Throwable cause) {
    super(cause);
  }

  public CsvParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
