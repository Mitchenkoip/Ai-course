package ru.kata.spring.boot_security.demo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class WebSecurityConfig {
    private static final String ADMIN_ROLE = "ADMIN";
    @SuppressWarnings("java:S1075") // Internal route; it must match AuthController and the UI contract.
    private static final String LOGIN_PATH = "/auth/login";
    @SuppressWarnings("java:S1075") // Internal API route; it must match UserRestController mappings.
    private static final String USERS_PATH = "/users/**";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:8080"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-XSRF-TOKEN"));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(LOGIN_PATH, "/process_login", "/static/**", "/webjars/**", "/error").permitAll()
                .requestMatchers("/admin", "/admin/**").hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST, USERS_PATH).hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.PATCH, USERS_PATH).hasRole(ADMIN_ROLE)
                .requestMatchers(HttpMethod.DELETE, USERS_PATH).hasRole(ADMIN_ROLE)
                .requestMatchers(USERS_PATH).authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage(LOGIN_PATH)
                .loginProcessingUrl("/process_login")
                .usernameParameter("email")
                .passwordParameter("password")
                .failureUrl("/auth/login?error")
                .successHandler((request, response, authentication) -> {
                    boolean isAdministrator = authentication.getAuthorities().stream()
                            .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + ADMIN_ROLE));
                    response.sendRedirect(isAdministrator ? "/admin" : "/user");
                }).permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(LOGIN_PATH)
                .permitAll());
        return http.build();
    }
}
