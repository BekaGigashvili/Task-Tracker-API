package com.javaprojects.tasktrackerapi.service;

import com.javaprojects.tasktrackerapi.dto.TaskDTO;
import com.javaprojects.tasktrackerapi.dto.TaskResponseDTO;
import com.javaprojects.tasktrackerapi.entity.*;
import com.javaprojects.tasktrackerapi.exceptions.TaskAccessException;
import com.javaprojects.tasktrackerapi.exceptions.TaskNotFoundException;
import com.javaprojects.tasktrackerapi.mapper.TaskMapper;
import com.javaprojects.tasktrackerapi.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final ProjectService projectService;
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Transactional
    public void deleteTask(Long taskId, User currentUser) {
        Task task = findById(taskId);

        checkCorrectManagerOrAdminAccess(currentUser, task.getProject());

        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponseDTO updateTaskDetails(Long taskId, TaskDTO taskDTO, User currentUser) {
        Task task = findById(taskId);

        checkCorrectManagerOrAdminAccess(currentUser, task.getProject());

        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setDueDate(taskDTO.getDueDate());
        task.setPriority(Priority.from(taskDTO.getPriority()));
        task.setUpdateDate(LocalDateTime.now());

        return taskMapper.toDto(task);
    }

    public Page<TaskResponseDTO> getTasks(
            String projectName,
            String assignedUserEmail,
            String statusStr,
            String priorityStr,
            int page,
            int size,
            User currentUser
    ) {
        if (projectName == null && assignedUserEmail == null) {
            throw new IllegalArgumentException("You must provide either a project name or an assigned user email for filtering.");
        }

        Project project = null;
        if (projectName != null) {
            project = projectService.findByName(projectName);
        }

        User assignedUser = null;
        if (assignedUserEmail != null) {
            assignedUser = userService.findByEmail(assignedUserEmail);
        }

        Status status = statusStr != null ? Status.from(statusStr) : null;
        Priority priority = priorityStr != null ? Priority.from(priorityStr) : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasksPage = switch (currentUser.getRole()) {
            case ADMIN -> taskRepository.findTasksFiltered(project, assignedUser, status, priority, pageable);
            case MANAGER -> {
                if (project != null && !project.getOwner().getId().equals(currentUser.getId())) {
                    throw new TaskAccessException("You are not allowed to view tasks for this project");
                }
                yield taskRepository.findTasksFiltered(project, assignedUser, status, priority, pageable);
            }
            case USER -> {
                if (assignedUser != null && !assignedUser.getId().equals(currentUser.getId())) {
                    throw new TaskAccessException("You are not allowed to view tasks for this user");
                }
                yield taskRepository.findTasksFiltered(null, currentUser, status, priority, pageable);
            }
            default -> throw new TaskAccessException("Unknown role: access denied");
        };

        return tasksPage.map(taskMapper::toDto);
    }

    public TaskResponseDTO createTask(TaskDTO taskDTO, User currentUser) {
        Project project = projectService.findByName(taskDTO.getProject());

        checkCorrectManagerOrAdminAccess(currentUser, project);

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(Status.TODO);
        task.setDueDate(taskDTO.getDueDate());
        task.setPriority(Priority.from(taskDTO.getPriority()));
        task.setProject(project);

        LocalDateTime now = LocalDateTime.now();

        task.setCreateDate(now);
        task.setUpdateDate(now);

        Task savedTask = taskRepository.save(task);

        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskResponseDTO assignTask(
            Long taskId,
            String userToAssignEmail,
            User currentUser
    ) {
        Task taskToBeAssigned = findById(taskId);

        Project project = taskToBeAssigned.getProject();

        checkCorrectManagerOrAdminAccess(currentUser, project);

        User userToAssign = userService.findByEmail(userToAssignEmail);

        taskToBeAssigned.setAssignedUser(userToAssign);
        taskToBeAssigned.setUpdateDate(LocalDateTime.now());

        Task savedTask = taskRepository.save(taskToBeAssigned);

        return taskMapper.toDto(savedTask);
    }

    @Transactional
    public TaskResponseDTO updateTaskStatus(User currentUser, Long taskId, String status) {
        Task task = findById(taskId);
        if (task.getAssignedUser() == null ||
                !task.getAssignedUser().getId().equals(currentUser.getId())) {
            throw new TaskAccessException("You are not allowed to set status to this task!");
        }
        task.setStatus(Status.from(status));
        task.setUpdateDate(LocalDateTime.now());
        return taskMapper.toDto(task);
    }

    private boolean ownsProject(User currentUser, Project project) {
        return project.getOwner().getId().equals(currentUser.getId());
    }

    private boolean isAdmin(User currentUser) {
        return currentUser.getRole().equals(Role.ADMIN);
    }

    private void checkCorrectManagerOrAdminAccess(User currentUser, Project project) {
        if (!ownsProject(currentUser, project) && !isAdmin(currentUser)) {
            throw new TaskAccessException("You are not allowed to perform this action on the task");
        }
    }

    private Task findById(Long id) {
        return taskRepository
                .findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found!"));
    }
}
