package com.inventory.service;

import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Organization;
import com.inventory.model.Role;
import com.inventory.model.Shop;
import com.inventory.model.User;
import com.inventory.repository.OrganizationRepository;
import com.inventory.repository.ShopEmployeeRepository;
import com.inventory.repository.ShopManagerRepository;
import com.inventory.repository.ShopRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private ShopEmployeeRepository shopEmployeeRepository;
    @Mock
    private ShopManagerRepository shopManagerRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShopService shopService;

    private Organization org;
    private User orgAdmin;

    @BeforeEach
    void setUp() {
        org = Organization.builder().id(1L).name("Acme").build();
        orgAdmin = User.builder().id(1L).username("admin").build();
        orgAdmin.setOrganization(org);
        Role r = Role.builder().id(1L).name("ROLE_ORGANIZATION_ADMIN").build();
        orgAdmin.getRoles().add(r);
    }

    @Test
    void getAccessibleShops_asOrganizationAdmin_returnsOrgShops() {
        Shop s = Shop.builder().id(10L).name("Shop A").organization(org).build();
        when(shopRepository.findByOrganization(org)).thenReturn(List.of(s));

        List<Shop> result = shopService.getAccessibleShops(orgAdmin);

        assertThat(result).containsExactly(s);
        verify(shopRepository).findByOrganization(org);
    }

    @Test
    void getAccessibleShops_asShopManager_returnsAssignedShop() {
        User manager = User.builder().id(2L).username("mgr").build();
        Role rm = Role.builder().name("ROLE_SHOP_MANAGER").build();
        manager.getRoles().add(rm);
        Shop assigned = Shop.builder().id(20L).name("Assigned").build();
        manager.setShop(assigned);

        List<Shop> result = shopService.getAccessibleShops(manager);

        assertThat(result).containsExactly(assigned);
    }

    @Test
    void createForOwner_withoutOrganization_throws() {
        User owner = User.builder().id(3L).username("noorg").build();

        assertThatThrownBy(() -> shopService.createForOwner("X", owner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Owner account must belong to an organization");
    }

    @Test
    void createForOwner_withOrganization_savesShop() {
        User owner = User.builder().id(4L).username("own").organization(org).build();
        Shop saved = Shop.builder().id(99L).name("New Shop").organization(org).build();
        when(shopRepository.save(any(Shop.class))).thenReturn(saved);

        Shop result = shopService.createForOwner("New Shop", owner);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getOrganization()).isEqualTo(org);
        verify(shopRepository).save(any(Shop.class));
    }

}
