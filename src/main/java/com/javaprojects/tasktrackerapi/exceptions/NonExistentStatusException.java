package com.javaprojects.tasktrackerapi.exceptions;

public class NonExistentStatusException extends RuntimeException {
    public NonExistentStatusException(String message) {
        super(message);
    }
}
