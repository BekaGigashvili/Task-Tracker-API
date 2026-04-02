package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.controller.ProjectController;
import com.javaprojects.tasktrackerapi.dto.ProjectDTO;
import com.javaprojects.tasktrackerapi.entity.Project;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.UserNotFoundException;
import com.javaprojects.tasktrackerapi.service.ProjectService;
import com.javaprojects.tasktrackerapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectController projectController;

    private User mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setEmail("user@example.com");

        when(authentication.getName()).thenReturn("user@example.com");
        when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    void getAllProjects_returnsProjectList() {
        List<Project> projects = List.of(new Project(), new Project());
        when(projectService.getAllProjects(mockUser)).thenReturn(projects);

        List<Project> result = projectController.getAllProjects(authentication);

        assertEquals(2, result.size());
        verify(projectService, times(1)).getAllProjects(mockUser);
    }

    @Test
    void getProjectByName_returnsProject() {
        Project project = new Project();
        project.setName("Project1");
        when(projectService.getProjectByName("Project1", mockUser)).thenReturn(project);

        ResponseEntity<Project> response = projectController.getProjectByName("Project1", authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(project, response.getBody());
    }

    @Test
    void getProjectByName_userNotFound_throwsException() {
        when(userService.findByEmail("user@example.com")).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () ->
                projectController.getProjectByName("Project1", authentication)
        );
    }

    @Test
    void createProject_returnsCreatedProject() {
        ProjectDTO dto = new ProjectDTO();
        Project createdProject = new Project();
        createdProject.setName("NewProject");

        when(projectService.createProject(dto, mockUser)).thenReturn(createdProject);

        ResponseEntity<Project> response = projectController.createProject(dto, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(createdProject, response.getBody());
        verify(projectService, times(1)).createProject(dto, mockUser);
    }

    @Test
    void updateProject_returnsUpdatedProject() {
        ProjectDTO dto = new ProjectDTO();
        Project updatedProject = new Project();
        updatedProject.setName("UpdatedProject");

        when(projectService.updateProject("Project1", dto, mockUser)).thenReturn(updatedProject);

        ResponseEntity<Project> response = projectController.updateProject("Project1", dto, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedProject, response.getBody());
    }

    @Test
    void deleteProject_callsServiceAndReturnsOk() {
        ResponseEntity<?> response = projectController.deleteProject("Project1", authentication);

        assertEquals(200, response.getStatusCode().value());
        verify(projectService, times(1)).deleteProject("Project1", mockUser);
    }
}