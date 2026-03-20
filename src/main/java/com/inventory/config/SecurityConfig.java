package com.inventory.config;

import com.inventory.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/error/**").permitAll()
                .requestMatchers("/admin/logs").hasRole("ORGANIZATION_ADMIN")
                .requestMatchers("/admin/**").hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")
                .requestMatchers("/organizations/**").hasRole("ORGANIZATION_ADMIN")
                .requestMatchers("/shops/**").hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")
                .requestMatchers("/reports/**").hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")
                .requestMatchers("/catalog/**").hasAnyRole("EMPLOYEE", "SHOP_MANAGER", "ORGANIZATION_ADMIN", "CUSTOMER")
                .requestMatchers("/sales/**").hasAnyRole("EMPLOYEE", "SHOP_MANAGER", "ORGANIZATION_ADMIN")
                .requestMatchers("/products/**", "/customers/**", "/suppliers/**", "/categories/**")
                .hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            );

        return http.build();
    }
}
