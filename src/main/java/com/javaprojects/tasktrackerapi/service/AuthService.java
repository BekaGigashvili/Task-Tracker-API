package com.javaprojects.tasktrackerapi.service;

import com.javaprojects.tasktrackerapi.entity.Role;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.UserAlreadyRegisteredException;
import com.javaprojects.tasktrackerapi.repository.UserRepository;
import com.javaprojects.tasktrackerapi.security.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegistrationRequest request){
        String email = request.getEmail();
        Optional<User> existingUser = userRepository.findByEmail(email);

        if(existingUser.isPresent()){
            throw new UserAlreadyRegisteredException("User with this email has already registered!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.from(request.getRole()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreateDate(now);
        user.setUpdateDate(now);
        userRepository.save(user);
    }
}
