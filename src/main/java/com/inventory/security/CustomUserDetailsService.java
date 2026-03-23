// Define the package for this class.
package com.inventory.security;

import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
// Define a public class.
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                // Set a configuration key and value.
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        // Check a condition before running code.
        if (!user.isActive()) {
            // Throw an exception for an error case.
            throw new DisabledException("Account is deactivated. Please contact an administrator.");
        // Close the current code block.
        }

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                // Set a configuration key and value.
                .map(Role::getName)
                // Set a configuration key and value.
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        // Return a value from this method.
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    // Close the current code block.
    }
// Close the current code block.
}
