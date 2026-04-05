package com.inventory.integration;

import com.inventory.model.Organization;
import com.inventory.model.Role;
import com.inventory.model.Shop;
import com.inventory.model.User;
import com.inventory.repository.OrganizationRepository;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.ShopRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.TestDatabaseAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("postgres-test")
@ImportAutoConfiguration(exclude = TestDatabaseAutoConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationContextIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void contextLoads() {
        // Verifies the full Spring context can start with the test profile.
    }

    @Test
    @Transactional
    void postgres_shouldPersistAndReadUserShopAndRole() {
        String token = UUID.randomUUID().toString().substring(0, 8);

        Organization org = organizationRepository.save(
            Organization.builder().name("Org-" + token).build()
        );

        Shop shop = shopRepository.save(
            Shop.builder().name("Shop-" + token).organization(org).build()
        );

        Role employeeRole = roleRepository.findByName("ROLE_EMPLOYEE")
            .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_EMPLOYEE").build()));

        User user = User.builder()
            .username("staff-" + token)
            .email("staff." + token + "@test.local")
            .password("noop")
            .active(true)
            .organization(org)
            .shop(shop)
            .build();
        user.getRoles().add(employeeRole);
        userRepository.save(user);

        User loaded = userRepository.findByEmail(user.getEmail()).orElseThrow();

        assertThat(loaded.getOrganization()).isNotNull();
        assertThat(loaded.getShop()).isNotNull();
        assertThat(loaded.getShop().getId()).isEqualTo(shop.getId());
        assertThat(loaded.getRoles()).extracting(Role::getName).contains("ROLE_EMPLOYEE");
        assertThat(userRepository.findByShopId(shop.getId()))
            .extracting(User::getEmail)
            .contains(user.getEmail());
    }
}
