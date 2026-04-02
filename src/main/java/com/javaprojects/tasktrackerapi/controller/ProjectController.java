package com.javaprojects.tasktrackerapi.controller;

import com.javaprojects.tasktrackerapi.dto.ProjectDTO;
import com.javaprojects.tasktrackerapi.dto.ProjectResponseDTO;
import com.javaprojects.tasktrackerapi.entity.Project;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.UserNotFoundException;
import com.javaprojects.tasktrackerapi.service.ProjectService;
import com.javaprojects.tasktrackerapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    private User getCurrentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<ProjectResponseDTO> getAllProjects(Authentication authentication) {
        return projectService.getAllProjects(getCurrentUser(authentication));
    }

    @GetMapping("/{name}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ProjectResponseDTO> getProjectByName(
            @PathVariable String name,
            Authentication authentication
    ) {
        ProjectResponseDTO project = projectService.getProjectByName(name, getCurrentUser(authentication));
        return ResponseEntity.ok(project);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ProjectResponseDTO> createProject(
            @RequestBody ProjectDTO projectRequest,
            Authentication authentication
    ) {
        ProjectResponseDTO project = projectService
                .createProject(projectRequest, getCurrentUser(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(project);
    }

    @PutMapping("/{name}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ProjectResponseDTO> updateProject(@PathVariable String name,
                                 @RequestBody ProjectDTO dto,
                                 Authentication authentication) {
        ProjectResponseDTO project = projectService.updateProject(name, dto, getCurrentUser(authentication));
        return ResponseEntity.status(HttpStatus.OK).body(project);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable String name, Authentication authentication) {
        projectService.deleteProject(name, getCurrentUser(authentication));
        return ResponseEntity.ok().build();
    }
}