package com.inventory.controller;

import com.inventory.model.Role;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.inventory.InventoryApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Ensure roles exist for tests
        if (roleRepository.findByName("ROLE_SHOP_MANAGER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_SHOP_MANAGER").build());
        }
        if (roleRepository.findByName("ROLE_ORGANIZATION_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ORGANIZATION_ADMIN").build());
        }
        if (roleRepository.findByName("ROLE_EMPLOYEE").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_EMPLOYEE").build());
        }
        // Clean up test users
        userRepository.findByEmail("testuser@integration.com")
                .ifPresent(u -> userRepository.delete(u));
    }

    // Integration Test 1: Valid registration redirects to login
    @Test
    void register_withValidData_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .param("username", "testuser")
                .param("email", "testuser@integration.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "ROLE_SHOP_MANAGER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));
    }

    // Integration Test 2: Duplicate email stays on register form
    @Test
    void register_withDuplicateEmail_shouldStayOnRegisterForm() throws Exception {
        // First registration
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .param("username", "firstuser")
                .param("email", "testuser@integration.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("role", "ROLE_SHOP_MANAGER"));

        // Attempt duplicate
        mockMvc.perform(post("/auth/register")
                .with(csrf())
                .param("username", "seconduser")
                .param("email", "testuser@integration.com")
                .param("password", "password456")
                .param("confirmPassword", "password456")
                .param("role", "ROLE_SHOP_MANAGER"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    // Integration Test 3: GET login page returns 200
    @Test
    void getLoginPage_shouldReturn200() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }
}
