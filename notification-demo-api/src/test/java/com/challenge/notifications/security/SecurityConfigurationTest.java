package com.challenge.notifications.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void shouldCreateUserWithSenderRoleAndEncodedPassword() {
        PasswordEncoder passwordEncoder = configuration.passwordEncoder();
        UserDetailsService userDetailsService = configuration.userDetailsService(
                passwordEncoder,
                "api-user",
                "secret-password"
        );

        UserDetails user = userDetailsService.loadUserByUsername("api-user");

        assertThat(user.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_NOTIFICATION_SENDER");
        assertThat(passwordEncoder.matches("secret-password", user.getPassword())).isTrue();
    }
}
