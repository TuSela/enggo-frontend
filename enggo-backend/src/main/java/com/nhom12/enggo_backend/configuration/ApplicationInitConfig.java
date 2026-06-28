package com.nhom12.enggo_backend.configuration;

import com.nhom12.enggo_backend.constant.PredefinedRole;
import com.nhom12.enggo_backend.entity.identity.auth.Role;
import com.nhom12.enggo_backend.entity.identity.User;
import com.nhom12.enggo_backend.repository.RoleRepository;
import com.nhom12.enggo_backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_USER_NAME = "admin";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    @ConditionalOnProperty(
            prefix = "spring.datasource",
            name = "driver-class-name",
            havingValue = "com.mysql.cj.jdbc.Driver")
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        log.info("Initializing application.....");
        return args -> {
            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                roleRepository.findByRoleName(PredefinedRole.USER_ROLE).orElseGet(() ->
                        roleRepository.save(Role.builder()
                                .roleName(PredefinedRole.USER_ROLE)
                                .roleDescription("User role")
                                .build()));

                Role adminRole = roleRepository.findByRoleName(PredefinedRole.ADMIN_ROLE).orElseGet(() ->
                        roleRepository.save(Role.builder()
                                .roleName(PredefinedRole.ADMIN_ROLE)
                                .roleDescription("Admin role")
                                .build()));

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .email("admin@enggo.local")
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .exp(0)
                        .level(1)
                        .streakDays(0)
                        .completedTasks(0)
                        .pvpWins(0)
                        .status("active")
                        .roles(roles)
                        .build();

                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it");
            }

            log.info("Application initialization completed .....");
        };
    }
}
