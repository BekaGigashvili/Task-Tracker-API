package com.javaprojects.tasktrackerapi.repository;

import com.javaprojects.tasktrackerapi.entity.Project;
import com.javaprojects.tasktrackerapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByName(String name);
    void deleteByName(String name);
    boolean existsByName(String name);
}
