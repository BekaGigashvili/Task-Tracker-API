package com.javaprojects.tasktrackerapi.dto;

import com.javaprojects.tasktrackerapi.entity.Priority;
import com.javaprojects.tasktrackerapi.entity.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TaskDTO {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private LocalDate dueDate;
    @NotBlank
    private String priority;
    @NotBlank
    private String project;
}
