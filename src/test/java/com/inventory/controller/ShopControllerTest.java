package com.inventory.controller;

import com.inventory.model.Role;
import com.inventory.model.Shop;
import com.inventory.model.User;
import com.inventory.service.ShopService;
import com.inventory.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopControllerTest {

    @Mock
    ShopService shopService;
    @Mock
    UserService userService;
    @Mock
    UserDetails principal;
    @Mock
    Model model;

    @InjectMocks
    ShopController controller;

    @Test
    void list_asOrganizationAdmin_returnsShops() {
        User owner = User.builder().email("admin@x.com").build();
        owner.setOrganization(null); // ensure ensureOwnerOrganization may set it later
        Role r = Role.builder().name("ROLE_ORGANIZATION_ADMIN").build();
        owner.getRoles().add(r);

        Shop s = Shop.builder().id(1L).name("S1").build();
        when(principal.getUsername()).thenReturn("admin@x.com");
        when(userService.getByEmail("admin@x.com")).thenReturn(owner);
        when(userService.ensureOwnerOrganization(owner)).thenReturn(owner);
        when(shopService.getAccessibleShops(owner)).thenReturn(Collections.singletonList(s));

        String view = controller.list(principal, model);

        assertThat(view).isEqualTo("admin/shops");
    }
}
