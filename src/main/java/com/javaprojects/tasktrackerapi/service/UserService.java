package com.javaprojects.tasktrackerapi.service;

import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.UserNotFoundException;
import com.javaprojects.tasktrackerapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
