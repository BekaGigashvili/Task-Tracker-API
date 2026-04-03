package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.dto.TaskDTO;
import com.javaprojects.tasktrackerapi.dto.TaskResponseDTO;
import com.javaprojects.tasktrackerapi.entity.*;
import com.javaprojects.tasktrackerapi.exceptions.TaskAccessException;
import com.javaprojects.tasktrackerapi.mapper.TaskMapper;
import com.javaprojects.tasktrackerapi.repository.TaskRepository;
import com.javaprojects.tasktrackerapi.service.ProjectService;
import com.javaprojects.tasktrackerapi.service.TaskService;
import com.javaprojects.tasktrackerapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private TaskMapper taskMapper;

    private User adminUser;
    private User managerUser;
    private User normalUser;
    private Project project;
    private Task task;
    private TaskDTO taskDTO;
    private TaskResponseDTO taskResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        managerUser = new User();
        managerUser.setId(2L);
        managerUser.setRole(Role.MANAGER);

        normalUser = new User();
        normalUser.setId(3L);
        normalUser.setRole(Role.USER);

        project = new Project();
        project.setId(100L);
        project.setOwner(managerUser);

        task = new Task();
        task.setId(10L);
        task.setTitle("Sample Task");
        task.setProject(project);
        task.setAssignedUser(normalUser);
        task.setStatus(Status.TODO);

        taskDTO = new TaskDTO();
        taskDTO.setTitle("Sample Task");
        taskDTO.setDescription("Description");
        taskDTO.setPriority("HIGH");
        taskDTO.setDueDate(LocalDate.now());
        taskDTO.setProject("Sample Project");

        taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setTitle("Sample Task");
    }

    @Test
    void createTask_managerCanCreateTask() {
        when(projectService.findByName(taskDTO.getProject())).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.createTask(taskDTO, managerUser);

        assertEquals(taskResponseDTO.getTitle(), result.getTitle());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_userCannotCreateTask_throwsException() {
        when(projectService.findByName(taskDTO.getProject())).thenReturn(project);

        assertThrows(TaskAccessException.class, () ->
                taskService.createTask(taskDTO, normalUser));
    }

    @Test
    void assignTask_adminCanAssign() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.findByEmail("user@example.com")).thenReturn(normalUser);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.assignTask(task.getId(), "user@example.com", adminUser);

        assertEquals(taskResponseDTO.getTitle(), result.getTitle());
        verify(taskRepository).save(task);
    }

    @Test
    void assignTask_userCannotAssign_throwsException() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(TaskAccessException.class, () ->
                taskService.assignTask(task.getId(), "someone@example.com", normalUser));
    }

    @Test
    void updateTaskStatus_assignedUserCanUpdate() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        TaskResponseDTO result = taskService.updateTaskStatus(normalUser, task.getId(), "IN_PROGRESS");

        assertEquals(taskResponseDTO.getTitle(), result.getTitle());
        assertEquals(Status.IN_PROGRESS, task.getStatus());
    }

    @Test
    void updateTaskStatus_otherUserCannotUpdate_throwsException() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(TaskAccessException.class, () ->
                taskService.updateTaskStatus(managerUser, task.getId(), "DONE"));
    }

    @Test
    void deleteTask_adminCanDelete() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.deleteTask(task.getId(), adminUser);

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_userCannotDelete_throwsException() {
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        assertThrows(TaskAccessException.class, () ->
                taskService.deleteTask(task.getId(), normalUser));
    }

    @Test
    void getTasks_adminCanSeeAll() {
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(projectService.findByName("SomeProject")).thenReturn(new Project());
        when(taskRepository.findTasksFiltered(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.getTasks(
                "SomeProject", null, null, null, 0, 10, adminUser);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_managerCannotSeeOtherProject_throwsException() {
        Project otherProject = new Project();
        otherProject.setId(200L);
        otherProject.setOwner(adminUser);

        when(projectService.findByName("Other Project")).thenReturn(otherProject);

        assertThrows(TaskAccessException.class, () ->
                taskService.getTasks("Other Project", null, null, null, 0, 10, managerUser));
    }

    @Test
    void getTasks_userCanSeeOnlyOwnTasks() {
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findTasksFiltered(null, normalUser, null, null, PageRequest.of(0, 10)))
                .thenReturn(taskPage);
        when(taskMapper.toDto(task)).thenReturn(taskResponseDTO);

        Page<TaskResponseDTO> result = taskService.getTasks(
                null, "normal@example.com", null, null, 0, 10, normalUser);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getTasks_userCannotSeeOthersTasks_throwsException() {
        User otherUser = new User();
        otherUser.setId(4L);
        otherUser.setEmail("other@example.com");

        when(userService.findByEmail("other@example.com")).thenReturn(otherUser);

        assertThrows(TaskAccessException.class, () ->
                taskService.getTasks(null, "other@example.com", null, null, 0, 10, normalUser));
    }
}