package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.entity.Role;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.NonExistentRoleException;
import com.javaprojects.tasktrackerapi.exceptions.UserAlreadyRegisteredException;
import com.javaprojects.tasktrackerapi.repository.UserRepository;
import com.javaprojects.tasktrackerapi.security.RegistrationRequest;
import com.javaprojects.tasktrackerapi.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldSaveNewUser_WhenEmailNotTaken() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("USER");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertNotNull(savedUser.getCreateDate());
        assertNotNull(savedUser.getUpdateDate());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyRegistered() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("USER");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        UserAlreadyRegisteredException ex = assertThrows(
                UserAlreadyRegisteredException.class,
                () -> authService.register(request)
        );

        assertEquals("User with this email has already registered!", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenRoleIsInvalid() {
        RegistrationRequest request = new RegistrationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("INVALID_ROLE");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(NonExistentRoleException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }
}