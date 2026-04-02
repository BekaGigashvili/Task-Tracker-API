package com.javaprojects.tasktrackerapi.mapper;

import com.javaprojects.tasktrackerapi.dto.ProjectDTO;
import com.javaprojects.tasktrackerapi.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    void updateProjectFromDto(ProjectDTO dto, @MappingTarget Project project);
    Project toEntity(ProjectDTO dto);
}
