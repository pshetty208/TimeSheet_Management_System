package org.tss.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.tss.dto.LoginRequest;
import org.tss.dto.LoginResponse;
import org.tss.dto.RegisterRequest;
import org.tss.exception.UnauthorizedException;
import org.tss.exception.ValidationException;
import org.tss.model.User;
import org.tss.repository.UserRepository;
import org.tss.security.JwtUtil;
import org.tss.service.UserService;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
            User user = userRepository.findByUsername(req.getUsername()).orElseThrow();
            var roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());
            String token = jwtUtil.generateToken(user.getUsername(), roles);
            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), roles));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        validateRole(req.getRole());
        User u = userService.register(req.getUsername(), req.getPassword(), req.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(u.getUsername());
    }

    private void validateRole(String role) {
        if (!isValidRole(role)) {
            throw new ValidationException("Invalid role: " + role + ". Valid roles are: EMPLOYEE, SUPERVISOR, ASSISTANT, SECRETARY, ADMINISTRATOR");
        }
    }

    private boolean isValidRole(String role) {
        return role != null && (role.equals("EMPLOYEE") || role.equals("SUPERVISOR") || role.equals("ASSISTANT") || role.equals("SECRETARY") || role.equals("ADMINISTRATOR"));
    }
}