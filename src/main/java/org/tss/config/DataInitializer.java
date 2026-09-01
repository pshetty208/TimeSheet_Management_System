package org.tss.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.tss.service.UserService;

@Component
@ConditionalOnProperty(name = "tss.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Value("${tss.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${tss.seed.admin.password:password123}")
    private String adminPassword;

    public DataInitializer(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        var admin = userService.findByUsername(adminUsername)
                .orElseGet(() -> userService.register(adminUsername, adminPassword, "ADMINISTRATOR"));
        admin.setConsent(true); admin.setUniversityStaff(true); admin.setPreferredLanguage("en");
        userService.save(admin);
    }
}
