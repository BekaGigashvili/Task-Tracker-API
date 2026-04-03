package com.javaprojects.tasktrackerapi.exceptions;

public class TaskAccessException extends RuntimeException {
    public TaskAccessException(String message) {
        super(message);
    }
}
