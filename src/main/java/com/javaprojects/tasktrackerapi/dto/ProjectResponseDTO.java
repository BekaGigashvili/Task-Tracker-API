package com.javaprojects.tasktrackerapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProjectResponseDTO {
    private String name;
    private String description;
    private UserDTO owner;
}
