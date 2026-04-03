package com.javaprojects.tasktrackerapi.dto;

import com.javaprojects.tasktrackerapi.entity.Priority;
import com.javaprojects.tasktrackerapi.entity.Project;
import com.javaprojects.tasktrackerapi.entity.Status;
import com.javaprojects.tasktrackerapi.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskResponseDTO {
    private String title;
    private String description;
    private Status status;
    private LocalDate dueDate;
    private Priority priority;
    private ProjectResponseDTO project;
    private UserDTO assignedUser;
}
