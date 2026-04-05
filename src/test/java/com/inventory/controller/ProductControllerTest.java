// Define the package for this class.
package com.inventory.controller;

import com.inventory.model.Role;
import com.inventory.model.Organization;
import com.inventory.model.Shop;
import com.inventory.model.User;
import com.inventory.repository.OrganizationRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.ShopRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// Define a class.
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ShopRepository shopRepository;
    @BeforeEach
    void setUp() {
        // Check a condition before running code.
        if (roleRepository.findByName("ROLE_SHOP_MANAGER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_SHOP_MANAGER").build());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleRepository.findByName("ROLE_ORGANIZATION_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ORGANIZATION_ADMIN").build());
        // Close the current code block.
        }
        if (roleRepository.findByName("ROLE_EMPLOYEE").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_EMPLOYEE").build());
        // Close the current code block.
        }

        Role managerRole = roleRepository.findByName("ROLE_SHOP_MANAGER").orElseThrow();
        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE").orElseThrow();
        User manager = userRepository.findByEmail("manager@test.local").orElseGet(() -> {
            User user = User.builder()
                    .username("manager")
                    .email("manager@test.local")
                    .password("noop")
                    .active(true)
                    .build();
            user.getRoles().add(managerRole);
            // Return a value from this method.
            return userRepository.save(user);
        });

        Organization org = organizationRepository.findByName("Test Org")
            .orElseGet(() -> organizationRepository.save(Organization.builder().name("Test Org").build()));
        Shop shop = shopRepository.findByNameAndOrganization("Test Shop", org)
            .orElseGet(() -> shopRepository.save(Shop.builder().name("Test Shop").organization(org).build()));

        manager.setOrganization(org);
        manager.setShop(shop);
        userRepository.save(manager);

        User employee = userRepository.findByEmail("employee@test.local").orElseGet(() -> {
            User user = User.builder()
                    .username("employee")
                    .email("employee@test.local")
                    .password("noop")
                    .active(true)
                    .build();
            user.getRoles().add(employeeRole);
            return userRepository.save(user);
        });
        employee.setOrganization(org);
        employee.setShop(shop);
        employee.setManager(false);
        userRepository.save(employee);
    // Close the current code block.
    }

    // Set a configuration key and value.
    // Integration Test 4: Unauthenticated access to /products redirects to login
    @Test
    void getProducts_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/auth/login"));
    // Close the current code block.
    }

    // Set a configuration key and value.
    // Integration Test 5: Authenticated SHOP_MANAGER can access /products
    @Test
    @WithMockUser(username = "manager@test.local", roles = "SHOP_MANAGER")
    void getProducts_asSeller_shouldReturn200() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"));
    // Close the current code block.
    }

    // Set a configuration key and value.
    // Integration Test 6: Authenticated SHOP_MANAGER can create a product
    @Test
    @WithMockUser(username = "manager@test.local", roles = "SHOP_MANAGER")
    void createProduct_asSeller_shouldRedirectToProducts() throws Exception {
        mockMvc.perform(post("/products")
                .with(csrf())
                .param("name", "Integration Test Product")
                .param("description", "Test description")
                .param("price", "29.99")
                .param("stockQuantity", "50")
                .param("lowStockThreshold", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));

        // Cleanup
        productRepository.findAll().stream()
                .filter(p -> "Integration Test Product".equals(p.getName()))
                // Set a configuration key and value.
                .forEach(productRepository::delete);
    // Close the current code block.
    }

    // Set a configuration key and value.
    // Integration Test 7: EMPLOYEE can access /products
    @Test
    @WithMockUser(username = "employee@test.local", roles = "EMPLOYEE")
    void getProducts_asEmployee_shouldReturn200() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"));
    // Close the current code block.
    }
// Close the current code block.
}
