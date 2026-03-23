package com.inventory.controller;

import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.RoleRepository;
import com.inventory.service.ShopService;
import com.inventory.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

	@Mock
	UserService userService;

	@Mock
	ShopService shopService;

	@Mock
	RoleRepository roleRepository;

	@Mock
	UserDetails principal;

	@Mock
	Model model;

	@InjectMocks
	AdminController controller;

	@Test
	void listUsers_whenActorHasNoOrgOrShop_setsEmptyUsersAndRoles() {
		var user = User.builder()
				.email("admin@example.com")
				.roles(Collections.emptySet())
				.organization(null)
				.shop(null)
				.build();

		when(principal.getUsername()).thenReturn("admin@example.com");
		when(userService.getByEmail("admin@example.com")).thenReturn(user);

		List<Role> expectedRoles = Collections.singletonList(
				Role.builder().id(1L).name("ROLE_EMPLOYEE").build()
		);

		when(roleRepository.findAll()).thenReturn(expectedRoles);

		String view = controller.listUsers(principal, model);

		assertEquals("admin/users", view);
		verify(model).addAttribute(eq("users"), eq(Collections.emptyList()));
		verify(model).addAttribute(eq("roles"), eq(expectedRoles));
	}
}

