package com.javaprojects.tasktrackerapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
}
