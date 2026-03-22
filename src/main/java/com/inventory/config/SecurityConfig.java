package com.inventory.config;

// custom service to load user from database
import com.inventory.security.CustomUserDetailsService;

// Lombok: constructor auto-generated
import lombok.RequiredArgsConstructor;

// Spring annotations
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// authentication related classes
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

// enable method-level security (@PreAuthorize etc.)
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// security config classes
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

// password encoding
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// filter chain for security
import org.springframework.security.web.SecurityFilterChain;


// These annotations tell Spring how to treat this class — without them, your security system mostly breaks.
@Configuration // marks this as config class
@EnableWebSecurity // enables Spring Security
@EnableMethodSecurity // allows role-based method security
@RequiredArgsConstructor
public class SecurityConfig {

    // custom class to fetch user from DB
    private final CustomUserDetailsService userDetailsService;

    // define password encoder (used to hash passwords)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // secure hashing algorithm
    }

    // connect Spring Security with our database user service
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService); // load user from DB
        provider.setPasswordEncoder(passwordEncoder());     // check password

        return provider;
    }

    // manages authentication (login process)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // main security configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // use our custom authentication provider
            .authenticationProvider(authenticationProvider())

            // define who can access which URL
            .authorizeHttpRequests(auth -> auth

                // public routes (no login needed)
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/error/**").permitAll()

                // only admin can access
                .requestMatchers("/admin/logs").hasRole("ORGANIZATION_ADMIN")

                // manager + admin
                .requestMatchers("/admin/**").hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")

                // only organization admin
                .requestMatchers("/organizations/**").hasRole("ORGANIZATION_ADMIN")

                // shop manager + admin
                .requestMatchers("/shops/**").hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")

                // reports access
                .requestMatchers("/reports/**").hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")

                // sales access
                .requestMatchers("/sales/**").hasAnyRole("EMPLOYEE", "SHOP_MANAGER", "ORGANIZATION_ADMIN")

                // product-related routes
                .requestMatchers("/products/**", "/categories/**")
                .hasAnyRole("SHOP_MANAGER", "ORGANIZATION_ADMIN")

                // all other requests need login
                .anyRequest().authenticated()
            )

            // login configuration
            .formLogin(form -> form
                .loginPage("/auth/login")              // custom login page
                .loginProcessingUrl("/auth/login")     // where login is processed
                .usernameParameter("email")           // login field name
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true) // after login success
                .failureUrl("/auth/login?error=true")  // if login fails
                .permitAll()
            )

            // logout configuration
            .logout(logout -> logout
                .logoutUrl("/auth/logout") // logout URL
                .logoutSuccessUrl("/auth/login?logout=true") // after logout
                .invalidateHttpSession(true) // destroy session
                .deleteCookies("JSESSIONID") // remove cookie
                .permitAll()
            )

            // CSRF protection (disable for h2 console)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            );

        // build and return security config
        return http.build();
    }
}