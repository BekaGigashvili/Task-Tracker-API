package com.javaprojects.tasktrackerapi.exceptions;

public class NonExistentRoleException extends RuntimeException {
    public NonExistentRoleException(String message) {
        super(message);
    }
}
