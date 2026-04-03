package com.javaprojects.tasktrackerapi.entity;


import com.javaprojects.tasktrackerapi.exceptions.NonExistentStatusException;

import java.util.Arrays;

public enum Status {
    TODO, IN_PROGRESS, DONE;

    public static Status from(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new NonExistentStatusException("Status " + value + " doesn't exist!")
                );
    }
}
