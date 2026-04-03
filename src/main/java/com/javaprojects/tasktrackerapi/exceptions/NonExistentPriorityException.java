package com.javaprojects.tasktrackerapi.exceptions;

public class NonExistentPriorityException extends RuntimeException {
    public NonExistentPriorityException(String message) {
        super(message);
    }
}
