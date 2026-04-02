package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.entity.Role;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.NonExistentRoleException;
import com.javaprojects.tasktrackerapi.exceptions.UserAlreadyRegisteredException;
import com.javaprojects.tasktrackerapi.exceptions.WrongEmailOrPasswordException;
import com.javaprojects.tasktrackerapi.repository.UserRepository;
import com.javaprojects.tasktrackerapi.security.AuthenticationRequest;
import com.javaprojects.tasktrackerapi.security.AuthenticationResponse;
import com.javaprojects.tasktrackerapi.security.CustomUserDetails;
import com.javaprojects.tasktrackerapi.security.RegistrationRequest;
import com.javaprojects.tasktrackerapi.service.AuthService;
import com.javaprojects.tasktrackerapi.service.JwtService;
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

    @Mock
    private JwtService jwtService;

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

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreCorrect() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("fake-jwt-token");

        AuthenticationResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.getToken());
        assertEquals(Role.USER, response.getRole());

        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(passwordEncoder, times(1)).matches("password123", "hashedPassword");
        verify(jwtService, times(1)).generateToken(any(CustomUserDetails.class));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        WrongEmailOrPasswordException ex = assertThrows(
                WrongEmailOrPasswordException.class,
                () -> authService.login(request)
        );

        assertEquals("Wrong email or password!", ex.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        WrongEmailOrPasswordException ex = assertThrows(
                WrongEmailOrPasswordException.class,
                () -> authService.login(request)
        );

        assertEquals("Wrong email or password!", ex.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }
}