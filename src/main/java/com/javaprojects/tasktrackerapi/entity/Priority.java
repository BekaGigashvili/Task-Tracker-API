package com.javaprojects.tasktrackerapi.entity;

import com.javaprojects.tasktrackerapi.exceptions.NonExistentPriorityException;

import java.util.Arrays;

public enum Priority {
    LOW, MEDIUM, HIGH;

    public static Priority from(String value) {
        return Arrays.stream(values())
                .filter(p -> p.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new NonExistentPriorityException("Priority " + value + " doesn't exist!")
                );
    }
}
