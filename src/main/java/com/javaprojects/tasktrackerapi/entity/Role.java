package com.javaprojects.tasktrackerapi.entity;

import com.javaprojects.tasktrackerapi.exceptions.NonExistentRoleException;

import java.util.Arrays;

public enum Role {
    ADMIN, MANAGER, USER;

    public static Role from(String value) {
        return Arrays.stream(values())
                .filter(r -> r.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new NonExistentRoleException("Role " + value + " doesn't exist!")
                );
    }
}
