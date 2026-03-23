package com.inventory.controller;

import com.inventory.model.Organization;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.service.OrganizationService;
import com.inventory.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrganizationControllerTest {

    @Mock
    OrganizationService organizationService;
    @Mock
    UserService userService;
    @Mock
    UserDetails principal;
    @Mock
    Model model;

    @InjectMocks
    OrganizationController controller;

    @Test
    void list_whenOwnerHasNoOrg_setsEmptyList() {
        User owner = User.builder().email("u@test.com").roles(Collections.emptySet()).organization(null).build();
        when(principal.getUsername()).thenReturn("u@test.com");
        when(userService.getByEmail("u@test.com")).thenReturn(owner);

        String view = controller.list(principal, model);

        assertEquals("admin/organizations", view);
        verify(model).addAttribute(eq("organizations"), eq(Collections.emptyList()));
    }
}
