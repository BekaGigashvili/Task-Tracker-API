package com.javaprojects.tasktrackerapi.mapper;

import com.javaprojects.tasktrackerapi.dto.UserDTO;
import com.javaprojects.tasktrackerapi.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(User user);
}
