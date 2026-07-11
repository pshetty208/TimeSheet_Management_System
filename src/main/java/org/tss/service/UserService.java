package org.tss.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tss.exception.ValidationException;
import org.tss.model.Role;
import org.tss.model.User;
import org.tss.repository.UserRepository;
import org.tss.repository.RoleRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User register(String username, String rawPassword, String roleName) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new ValidationException("Username must be between 3 and 50 characters");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ValidationException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        Role role = roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }
}