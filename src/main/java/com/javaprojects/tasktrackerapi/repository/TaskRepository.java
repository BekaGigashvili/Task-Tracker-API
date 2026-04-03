package com.javaprojects.tasktrackerapi.repository;

import com.javaprojects.tasktrackerapi.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t " +
            "WHERE (:project IS NULL OR t.project = :project) " +
            "AND (:assignedUser IS NULL OR t.assignedUser = :assignedUser) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority)")
    Page<Task> findTasksFiltered(@Param("project") Project project,
                                 @Param("assignedUser") User assignedUser,
                                 @Param("status") Status status,
                                 @Param("priority") Priority priority,
                                 Pageable pageable);

    void deleteByProject(Project project);
}
