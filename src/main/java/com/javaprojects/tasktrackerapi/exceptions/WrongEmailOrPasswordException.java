package com.javaprojects.tasktrackerapi.exceptions;

public class WrongEmailOrPasswordException extends RuntimeException {
  public WrongEmailOrPasswordException(String message) {
    super(message);
  }
}
