package com.tasktracker.cvs.exceptions;

public class CvsMapperException extends RuntimeException {
  public CvsMapperException(String message) {
    super(message);
  }

  public CvsMapperException(String message, Throwable cause) {
    super(message, cause);
  }
}
