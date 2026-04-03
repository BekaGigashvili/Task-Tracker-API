package com.javaprojects.tasktrackerapi.controller;

import com.javaprojects.tasktrackerapi.dto.TaskDTO;
import com.javaprojects.tasktrackerapi.dto.TaskResponseDTO;
import com.javaprojects.tasktrackerapi.entity.Task;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.service.TaskService;
import com.javaprojects.tasktrackerapi.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    private User getCurrentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TaskResponseDTO> createTask(
            @RequestBody @Valid TaskDTO taskDTO,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(taskDTO, currentUser));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TaskResponseDTO> updateTaskDetails(
            @PathVariable @NotNull Long taskId,
            @RequestBody @Valid TaskDTO taskDTO,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.status(HttpStatus.OK).body(taskService.updateTaskDetails(taskId, taskDTO, currentUser));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> deleteTask(
            @PathVariable @NotNull Long taskId,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        taskService.deleteTask(taskId, currentUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{taskId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TaskResponseDTO> assignTask(
            @PathVariable @NotNull Long taskId,
            @RequestParam @NotNull String userEmail,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.assignTask(taskId, userEmail, currentUser));
    }

    @PutMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MANAGER')")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable @NotNull Long taskId,
            @RequestParam @NotNull String status,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskService.updateTaskStatus(currentUser, taskId, status));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
    public Page<TaskResponseDTO> getTasks(
            @RequestParam(required = false) String projectName,
            @RequestParam(required = false) String assignedUserEmail,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        User currentUser = getCurrentUser(authentication);
        return taskService.getTasks(projectName, assignedUserEmail, status, priority, page, size, currentUser);
    }
}