package com.javaprojects.tasktrackerapi;

import com.javaprojects.tasktrackerapi.controller.AuthController;
import com.javaprojects.tasktrackerapi.security.AuthenticationRequest;
import com.javaprojects.tasktrackerapi.security.AuthenticationResponse;
import com.javaprojects.tasktrackerapi.security.RegistrationRequest;
import com.javaprojects.tasktrackerapi.service.AuthService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private static Validator validator;
    private static ValidatorFactory factory;

    @BeforeAll
    static void setupValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_validRequest_callsServiceAndReturnsCreated() {
        RegistrationRequest request = new RegistrationRequest("user@example.com", "password123", "USER");

        ResponseEntity<?> response = authController.register(request);

        verify(authService, times(1)).register(request);
        assertEquals(201, response.getStatusCode().value());
    }

    @Test
    void register_invalidEmail_failsValidation() {
        RegistrationRequest request = new RegistrationRequest("invalid-email", "password123", "USER");

        Set violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void register_blankPassword_failsValidation() {
        RegistrationRequest request = new RegistrationRequest("user@example.com", "", "USER");

        Set violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void login_validRequest_returnsAuthResponse() {
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "password123");
        AuthenticationResponse authResponse = new AuthenticationResponse("token123", null);

        when(authService.login(request)).thenReturn(authResponse);

        ResponseEntity<?> response = authController.login(request);

        verify(authService, times(1)).login(request);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(authResponse, response.getBody());
    }

    @Test
    void login_invalidEmail_failsValidation() {
        AuthenticationRequest request = new AuthenticationRequest("invalid-email", "password123");

        Set violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void login_blankPassword_failsValidation() {
        AuthenticationRequest request = new AuthenticationRequest("user@example.com", "");

        Set violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}