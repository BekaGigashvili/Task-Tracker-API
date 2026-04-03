package com.javaprojects.tasktrackerapi.mapper;

import com.javaprojects.tasktrackerapi.dto.TaskResponseDTO;
import com.javaprojects.tasktrackerapi.entity.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProjectMapper.class})
public interface TaskMapper {
    TaskResponseDTO toDto(Task task);
}
