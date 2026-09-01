package org.tss.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tss.exception.ResourceNotFoundException;
import org.tss.exception.ValidationException;
import org.tss.model.User;
import org.tss.repository.UserRepository;
import org.tss.dto.RegisterRequest;
import org.tss.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    private final UserService userService;
    public UserController(UserRepository users, UserService userService) { this.users = users; this.userService = userService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERVISOR','ASSISTANT','SECRETARY','ADMINISTRATOR')")
    public List<User> list() { return users.findAll(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public User create(@RequestBody RegisterRequest req) {
        if (!req.isConsent()) throw new ValidationException("Consent is required");
        User user = userService.register(req.getUsername(), req.getPassword(), req.getRole());
        AuthController.applyProfile(user, req, true);
        return users.save(user);
    }

    @GetMapping("/me")
    public User me(Authentication auth) { return users.findByUsername(auth.getName()).orElseThrow(); }

    @PutMapping("/me/preferences")
    public User preferences(Authentication auth, @RequestBody Map<String,Object> input) {
        User user = users.findByUsername(auth.getName()).orElseThrow();
        String language = String.valueOf(input.getOrDefault("preferredLanguage", "en"));
        if (!List.of("en", "de").contains(language)) throw new ValidationException("Language must be en or de");
        user.setPreferredLanguage(language);
        if (input.containsKey("consent")) user.setConsent(Boolean.TRUE.equals(input.get("consent")));
        return users.save(user);
    }
}
