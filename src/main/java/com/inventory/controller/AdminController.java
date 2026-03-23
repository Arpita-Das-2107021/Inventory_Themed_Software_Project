package com.inventory.controller;

import com.inventory.repository.RoleRepository;
import com.inventory.model.User;
// import com.inventory.service.AuditLogService;
import com.inventory.service.ShopService;
import com.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // marks this as web controller
@RequestMapping("/admin") // base URL = /admin
@PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER')") // restrict access
@RequiredArgsConstructor // auto constructor for final fields
public class AdminController {

    private final UserService userService; // user operations
    private final ShopService shopService; // shop operations
    private final RoleRepository roleRepository; // role data
    // private final AuditLogService auditLogService; // logging

    // ===== GET ALL USERS =====
    @GetMapping("/users")
    public String listUsers(@AuthenticationPrincipal UserDetails admin, Model model) {

        // get logged-in user
        User actor = userService.getByEmail(admin.getUsername());

        // check if org admin
        boolean isOwner = actor.getRoles().stream()
                .anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));

        // load users based on role
        if (isOwner && actor.getOrganization() != null) {
            model.addAttribute("users",
                    userService.getUsersByOrganization(actor.getOrganization().getId()));
        } else if (actor.getShop() != null) {
            model.addAttribute("users",
                    userService.getUsersByShop(actor.getShop().getId()));
        } else {
            model.addAttribute("users", java.util.List.of()); // no users
        }

        model.addAttribute("roles", roleRepository.findAll()); // send roles
        model.addAttribute("isOwner", isOwner); // send role flag

        return "admin/users"; // return UI page
    }

    // ===== SHOW CREATE USER FORM =====
    @GetMapping("/users/new")
    public String createUserForm(@AuthenticationPrincipal UserDetails admin, Model model) {

        User actor = userService.getByEmail(admin.getUsername());

        // check role
        boolean isOwner = actor.getRoles().stream()
                .anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));

        model.addAttribute("isOwner", isOwner);

        // show shop name if exists
        model.addAttribute("actorShopName",
                actor.getShop() != null ? actor.getShop().getName() : null);

        // only owner can see all shops
        if (isOwner && actor.getOrganization() != null) {
            model.addAttribute("shops",
                    shopService.getShopsByOrganization(actor.getOrganization().getId()));
        } else {
            model.addAttribute("shops", java.util.List.of());
        }

        return "admin/user-create"; // return form page
    }

    // ===== CREATE USER =====
    @PostMapping("/users/new")
    public String createUser(
            @RequestParam String username, // form input
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "ROLE_EMPLOYEE") String roleName,
            @RequestParam(required = false) Long shopId,
            @AuthenticationPrincipal UserDetails admin,
            RedirectAttributes redirectAttributes) {

        // check password length
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Password must be at least 6 characters.");
            return "redirect:/admin/users/new";
        }

        try {
            User actor = userService.getByEmail(admin.getUsername());

            // check role
            boolean isOwner = actor.getRoles().stream()
                    .anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));

            // manager can only create employee
            String resolvedRole = isOwner ? roleName : "ROLE_EMPLOYEE";

            com.inventory.model.Shop resolvedShop = null;

            if (isOwner) {
                // shop required for employee/manager
                if ("ROLE_EMPLOYEE".equals(resolvedRole) ||
                        "ROLE_SHOP_MANAGER".equals(resolvedRole)) {

                    if (shopId == null) {
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "Please choose a shop.");
                        return "redirect:/admin/users/new";
                    }

                    resolvedShop = shopService.getById(shopId);

                    // check same organization
                    if (!actor.getOrganization().getId()
                            .equals(resolvedShop.getOrganization().getId())) {
                        throw new IllegalArgumentException("Invalid shop.");
                    }
                }
            } else {
                // manager must have a shop
                if (actor.getShop() == null) {
                    throw new IllegalArgumentException("Manager has no shop.");
                }
                resolvedShop = actor.getShop();
            }

            // create user in DB
            var newUser = userService.createUser(
                    username, email, password, resolvedRole,
                    actor.getOrganization(), resolvedShop);

            // assign employee to shop
            if (resolvedShop != null && "ROLE_EMPLOYEE".equals(resolvedRole)) {
                if (isOwner) {
                    shopService.assignEmployeeForOwner(
                            resolvedShop.getId(), newUser.getId(), actor);
                } else {
                    shopService.assignEmployeeForManager(
                            resolvedShop.getId(), newUser.getId(), actor);
                }
            }

            // assign manager to shop
            if (resolvedShop != null &&
                    "ROLE_SHOP_MANAGER".equals(resolvedRole) && isOwner) {
                shopService.assignManagerForOwner(
                        resolvedShop.getId(), newUser.getId(), actor);
            }

            // save log

            redirectAttributes.addFlashAttribute("successMessage",
                    "User created successfully.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users"; // go back to users page
    }

    // ===== EDIT USER FORM =====
    @GetMapping("/users/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String editUserForm(@PathVariable Long id, Model model) {

        model.addAttribute("editUser", userService.getById(id)); // load user
        model.addAttribute("roles", roleRepository.findAll()); // load roles

        return "admin/user-edit";
    }

    // ===== UPDATE USER =====
    @PostMapping("/users/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam String roleName,
            @AuthenticationPrincipal UserDetails admin,
            RedirectAttributes redirectAttributes) {

        userService.updateUser(id, username, roleName); // update

        // log update


        redirectAttributes.addFlashAttribute("successMessage",
                "User updated.");

        return "redirect:/admin/users";
    }

    // ===== TOGGLE ACTIVE =====
    @PostMapping("/users/{id}/toggle")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String toggleUserActive(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails admin,
            RedirectAttributes redirectAttributes) {

        var user = userService.toggleActive(id); // switch active/inactive

        String status = user.isActive() ? "activated" : "deactivated";

        // log action


        redirectAttributes.addFlashAttribute("successMessage",
                "User " + status);

        return "redirect:/admin/users";
    }

    // ===== RESET PASSWORD =====
    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserDetails admin,
            RedirectAttributes redirectAttributes) {

        // check password
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Password too short.");
            return "redirect:/admin/users/" + id + "/edit";
        }

        userService.resetPassword(id, newPassword); // reset password

        // log


        redirectAttributes.addFlashAttribute("successMessage",
                "Password reset done.");

        return "redirect:/admin/users";
    }

    // ===== AUDIT LOGS =====

}