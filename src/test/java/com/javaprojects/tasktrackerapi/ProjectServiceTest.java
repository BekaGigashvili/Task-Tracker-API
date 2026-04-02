package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.dto.ProjectDTO;
import com.javaprojects.tasktrackerapi.dto.ProjectResponseDTO;
import com.javaprojects.tasktrackerapi.entity.Project;
import com.javaprojects.tasktrackerapi.entity.Role;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.ProjectAccessException;
import com.javaprojects.tasktrackerapi.exceptions.ProjectNotFoundException;
import com.javaprojects.tasktrackerapi.mapper.ProjectMapper;
import com.javaprojects.tasktrackerapi.repository.ProjectRepository;
import com.javaprojects.tasktrackerapi.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private User adminUser;
    private User normalUser;
    private Project project;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRole(Role.USER);

        project = new Project();
        project.setId(100L);
        project.setName("Test Project");
        project.setOwner(normalUser);
        project.setCreateDate(LocalDateTime.now());
        project.setUpdateDate(LocalDateTime.now());
    }

    @Test
    void testGetAllProjectsAsAdmin() {
        when(projectRepository.findAll()).thenReturn(List.of(project));

        List<ProjectResponseDTO> projects = projectService.getAllProjects(adminUser);
        assertEquals(1, projects.size());
        verify(projectRepository).findAll();
    }

    @Test
    void testGetAllProjectsAsUser() {
        Project otherProject = new Project();
        otherProject.setId(101L);
        otherProject.setOwner(adminUser);
        otherProject.setName("Other Project");

        when(projectRepository.findAll()).thenReturn(List.of(project, otherProject));

        List<ProjectResponseDTO> projects = projectService.getAllProjects(normalUser);
        assertEquals(1, projects.size());
    }

    @Test
    void testGetProjectByNameAsNonOwner() {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setRole(Role.USER);

        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));

        ProjectResponseDTO found = projectService.getProjectByName("Test Project", otherUser);
        assertNull(found);
    }

    @Test
    void testCreateProjectSuccess() {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("New Project");
        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setName("New Project");

        Project newProject = new Project();
        newProject.setName(dto.getName());

        when(projectRepository.existsByName(dto.getName())).thenReturn(false);
        when(projectMapper.toEntity(dto)).thenReturn(newProject);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(projectMapper.toDto(newProject)).thenReturn(responseDTO);

        ProjectResponseDTO created = projectService.createProject(dto, normalUser);
        assertEquals(dto.getName(), created.getName());
    }

    @Test
    void testCreateProjectAlreadyExists() {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("Test Project");

        when(projectRepository.existsByName(dto.getName())).thenReturn(true);

        assertThrows(ProjectAccessException.class, () -> projectService.createProject(dto, normalUser));
    }

    @Test
    void testUpdateProjectSuccess() {
        ProjectDTO dto = new ProjectDTO();
        dto.setName("Updated Name");

        ProjectResponseDTO responseDTO = new ProjectResponseDTO();
        responseDTO.setName("Updated Name");

        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(responseDTO);

        ProjectResponseDTO updated = projectService.updateProject("Test Project", dto, normalUser);

        assertEquals("Updated Name", updated.getName());
        verify(projectMapper).updateProjectFromDto(dto, project);
    }

    @Test
    void testUpdateProjectNotFound() {
        when(projectRepository.findByName("Missing Project")).thenReturn(Optional.empty());
        ProjectDTO dto = new ProjectDTO();

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.updateProject("Missing Project", dto, normalUser));
    }

    @Test
    void testUpdateProjectAccessDenied() {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setRole(Role.USER);

        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));
        ProjectDTO dto = new ProjectDTO();

        assertThrows(ProjectAccessException.class,
                () -> projectService.updateProject("Test Project", dto, otherUser));
    }

    @Test
    void testDeleteProjectSuccess() {
        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));

        projectService.deleteProject("Test Project", normalUser);

        verify(projectRepository).delete(project);
    }

    @Test
    void testDeleteProjectNotFound() {
        when(projectRepository.findByName("Missing Project")).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class,
                () -> projectService.deleteProject("Missing Project", normalUser));
    }

    @Test
    void testDeleteProjectAccessDenied() {
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setRole(Role.USER);

        when(projectRepository.findByName("Test Project")).thenReturn(Optional.of(project));

        assertThrows(ProjectAccessException.class,
                () -> projectService.deleteProject("Test Project", otherUser));
    }
}