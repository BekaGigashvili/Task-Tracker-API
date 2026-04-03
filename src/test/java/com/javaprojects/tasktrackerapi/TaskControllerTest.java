package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.controller.TaskController;
import com.javaprojects.tasktrackerapi.dto.TaskDTO;
import com.javaprojects.tasktrackerapi.dto.TaskResponseDTO;
import com.javaprojects.tasktrackerapi.entity.Priority;
import com.javaprojects.tasktrackerapi.entity.Status;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.service.TaskService;
import com.javaprojects.tasktrackerapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    @InjectMocks
    private TaskController taskController;

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");
    }

    private TaskResponseDTO sampleTaskResponse() {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setTitle("Sample Task");
        dto.setDescription("Description");
        dto.setStatus(Status.TODO);
        dto.setDueDate(LocalDate.now().plusDays(5));
        dto.setPriority(Priority.HIGH);
        return dto;
    }

    private TaskDTO sampleTaskDTO() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Sample Task");
        dto.setDescription("Description");
        dto.setProject("Project1");
        dto.setPriority("HIGH");
        return dto;
    }

    @Test
    void testCreateTask() {
        TaskDTO dto = sampleTaskDTO();
        TaskResponseDTO responseDTO = sampleTaskResponse();

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(taskService.createTask(dto, mockUser)).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.createTask(dto, authentication);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(responseDTO, response.getBody());
        verify(taskService).createTask(dto, mockUser);
    }

    @Test
    void testUpdateTaskDetails() {
        TaskDTO dto = sampleTaskDTO();
        TaskResponseDTO responseDTO = sampleTaskResponse();

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(taskService.updateTaskDetails(1L, dto, mockUser)).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.updateTaskDetails(1L, dto, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDTO, response.getBody());
        verify(taskService).updateTaskDetails(1L, dto, mockUser);
    }

    @Test
    void testDeleteTask() {
        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        ResponseEntity<?> response = taskController.deleteTask(1L, authentication);

        assertEquals(204, response.getStatusCode().value());
        verify(taskService).deleteTask(1L, mockUser);
    }

    @Test
    void testAssignTask() {
        TaskResponseDTO responseDTO = sampleTaskResponse();

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(taskService.assignTask(1L, "assignee@example.com", mockUser)).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.assignTask(1L, "assignee@example.com", authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDTO, response.getBody());
        verify(taskService).assignTask(1L, "assignee@example.com", mockUser);
    }

    @Test
    void testUpdateTaskStatus() {
        TaskResponseDTO responseDTO = sampleTaskResponse();

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(taskService.updateTaskStatus(mockUser, 1L, "DONE")).thenReturn(responseDTO);

        ResponseEntity<TaskResponseDTO> response = taskController.updateTaskStatus(1L, "DONE", authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(responseDTO, response.getBody());
        verify(taskService).updateTaskStatus(mockUser, 1L, "DONE");
    }

    @Test
    void testGetTasks() {
        TaskResponseDTO task1 = sampleTaskResponse();
        Page<TaskResponseDTO> page = new PageImpl<>(List.of(task1));

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);
        when(taskService.getTasks(
                "Project1", "user@example.com", "TODO", "HIGH", 0, 10, mockUser
        )).thenReturn(page);

        Page<TaskResponseDTO> response = taskController.getTasks(
                "Project1", "user@example.com", "TODO", "HIGH", 0, 10, authentication
        );

        assertEquals(1, response.getTotalElements());
        assertEquals(task1, response.getContent().get(0));
        verify(taskService).getTasks("Project1", "user@example.com", "TODO", "HIGH", 0, 10, mockUser);
    }
}