// Define the package for this class.
package com.inventory.service;

import com.inventory.dto.RegisterRequest;
import com.inventory.exception.DuplicateEmailException;
import com.inventory.model.Organization;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.OrganizationRepository;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
// Define a class.
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;
    private RegisterRequest request;
    private Role ownerRole;
    @BeforeEach
    void setUp() {
        request = new RegisterRequest();
        request.setUsername("alice");
        request.setEmail("alice@test.com");
        request.setPassword("pass123");
        request.setConfirmPassword("pass123");
        request.setRole("ROLE_SHOP_MANAGER");

        ownerRole = Role.builder().id(1L).name("ROLE_ORGANIZATION_ADMIN").build();
    // Close the current code block.
    }

    // Test 12
    @Test
    void register_shouldEncodePasswordBeforeSaving() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_ORGANIZATION_ADMIN")).thenReturn(Optional.of(ownerRole));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode("pass123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getPassword()).isEqualTo("$2a$encoded");
        verify(passwordEncoder).encode("pass123");
    // Close the current code block.
    }

    // Test 13
    @Test
    void register_shouldAssignRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_ORGANIZATION_ADMIN")).thenReturn(Optional.of(ownerRole));
        when(organizationRepository.save(any(Organization.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(request);

        assertThat(result.getRoles()).contains(ownerRole);
        assertThat(result.getOrganization()).isNotNull();
    // Close the current code block.
    }

    // Test 14
    @Test
    void register_shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("alice@test.com");

        verify(userRepository, never()).save(any());
    // Close the current code block.
    }

    // Test 15
    @Test
    void findByEmail_shouldReturnUser() {
        User user = User.builder().id(1L).email("alice@test.com").build();
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.findByEmail("alice@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("alice@test.com");
    // Close the current code block.
    }
// Close the current code block.
}
