package com.javaprojects.tasktrackerapi.service;

import com.javaprojects.tasktrackerapi.dto.ProjectDTO;
import com.javaprojects.tasktrackerapi.entity.Project;
import com.javaprojects.tasktrackerapi.entity.Role;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.ProjectAccessException;
import com.javaprojects.tasktrackerapi.exceptions.ProjectNotFoundException;
import com.javaprojects.tasktrackerapi.mapper.ProjectMapper;
import com.javaprojects.tasktrackerapi.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public List<Project> getAllProjects(User currentUser) {
        if (currentUser.getRole().equals(Role.ADMIN)) {
            return projectRepository.findAll();
        } else {
            return projectRepository.findAll().stream()
                    .filter(p -> p.getOwner().getId().equals(currentUser.getId()))
                    .toList();
        }
    }

    public Project getProjectByName(String name, User currentUser) {
        Optional<Project> projectOpt = projectRepository.findByName(name);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            if (currentUser.getRole().equals(Role.ADMIN) || project.getOwner().getId().equals(currentUser.getId())) {
                return project;
            }
        }
        return null;
    }

    public Project createProject(ProjectDTO projectDTO, User currentUser) {
        if(projectRepository.existsByName(projectDTO.getName())){
            throw new ProjectAccessException("Project already exists");
        }
        Project project = projectMapper.toEntity(projectDTO);
        project.setOwner(currentUser);
        LocalDateTime now = LocalDateTime.now();
        project.setCreateDate(now);
        project.setUpdateDate(now);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(String name, ProjectDTO dto, User currentUser) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found!"));

        if (!currentUser.getRole().equals(Role.ADMIN) &&
                !project.getOwner().getId().equals(currentUser.getId())) {
            throw new ProjectAccessException("You are not allowed to update this project!");
        }

        projectMapper.updateProjectFromDto(dto, project);
        project.setUpdateDate(LocalDateTime.now());

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(String name, User currentUser) {
        Project project = projectRepository.findByName(name)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found with name " + name));

        if (!currentUser.getRole().equals(Role.ADMIN) &&
                !project.getOwner().getId().equals(currentUser.getId())) {
            throw new ProjectAccessException("You are not allowed to delete this project!");
        }

        projectRepository.delete(project);
    }
}