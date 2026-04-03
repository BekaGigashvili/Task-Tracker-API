package com.javaprojects.tasktrackerapi.service.auth;

import com.javaprojects.tasktrackerapi.entity.Role;
import com.javaprojects.tasktrackerapi.entity.User;
import com.javaprojects.tasktrackerapi.exceptions.UserAlreadyRegisteredException;
import com.javaprojects.tasktrackerapi.exceptions.WrongEmailOrPasswordException;
import com.javaprojects.tasktrackerapi.repository.UserRepository;
import com.javaprojects.tasktrackerapi.security.AuthenticationRequest;
import com.javaprojects.tasktrackerapi.security.AuthenticationResponse;
import com.javaprojects.tasktrackerapi.security.CustomUserDetails;
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
    private final JwtService jwtService;

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

    public AuthenticationResponse login(AuthenticationRequest request) {
        String email = request.getEmail();
        String exceptionMessage = "Wrong email or password!";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WrongEmailOrPasswordException(exceptionMessage));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new WrongEmailOrPasswordException(exceptionMessage);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthenticationResponse.builder()
                .token(token)
                .role(user.getRole())
                .build();
    }
}
