package com.inventory.controller;

import com.inventory.repository.RoleRepository;
import com.inventory.model.User;
import com.inventory.service.AuditLogService;
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

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ShopService shopService;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER')")
    public String listUsers(@AuthenticationPrincipal UserDetails admin, Model model) {
        User actor = userService.getByEmail(admin.getUsername());
        boolean isOwner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        if (isOwner && actor.getOrganization() != null) {
            model.addAttribute("users", userService.getUsersByOrganization(actor.getOrganization().getId()));
        } else if (actor.getShop() != null) {
            model.addAttribute("users", userService.getUsersByShop(actor.getShop().getId()));
        } else {
            model.addAttribute("users", java.util.List.of());
        }
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("isOwner", isOwner);
        return "admin/users";
    }

    @GetMapping("/users/new")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER')")
    public String createUserForm(@AuthenticationPrincipal UserDetails admin, Model model) {
        User actor = userService.getByEmail(admin.getUsername());
        boolean isOwner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("actorShopName", actor.getShop() != null ? actor.getShop().getName() : null);
        if (isOwner && actor.getOrganization() != null) {
            model.addAttribute("shops", shopService.getShopsByOrganization(actor.getOrganization().getId()));
        } else {
            model.addAttribute("shops", java.util.List.of());
        }
        return "admin/user-create";
    }

    @PostMapping("/users/new")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER')")
    public String createUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam(defaultValue = "ROLE_EMPLOYEE") String roleName,
                             @RequestParam(required = false) Long shopId,
                             @AuthenticationPrincipal UserDetails admin,
                             RedirectAttributes redirectAttributes) {
        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters.");
            return "redirect:/admin/users/new";
        }
        try {
            User actor = userService.getByEmail(admin.getUsername());
            boolean isOwner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));

            String resolvedRole = isOwner ? roleName : "ROLE_EMPLOYEE";
            com.inventory.model.Shop resolvedShop = null;
            if (isOwner) {
                if (("ROLE_EMPLOYEE".equals(resolvedRole) || "ROLE_SHOP_MANAGER".equals(resolvedRole))) {
                    if (shopId == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Please choose a shop for staff accounts.");
                        return "redirect:/admin/users/new";
                    }
                    resolvedShop = shopService.getById(shopId);
                    if (actor.getOrganization() == null || resolvedShop.getOrganization() == null ||
                            !actor.getOrganization().getId().equals(resolvedShop.getOrganization().getId())) {
                        throw new IllegalArgumentException("You can only assign staff to shops in your organization.");
                    }
                }
            } else {
                if (actor.getShop() == null) {
                    throw new IllegalArgumentException("Shop admin must be assigned to a shop before creating employees.");
                }
                resolvedShop = actor.getShop();
            }

            var newUser = userService.createUser(username, email, password, resolvedRole,
                isOwner ? actor.getOrganization() : actor.getOrganization(), resolvedShop);

            if (resolvedShop != null && "ROLE_EMPLOYEE".equals(resolvedRole)) {
                if (isOwner) {
                    shopService.assignEmployeeForOwner(resolvedShop.getId(), newUser.getId(), actor);
                } else {
                    shopService.assignEmployeeForManager(resolvedShop.getId(), newUser.getId(), actor);
                }
            }

            if (resolvedShop != null && "ROLE_SHOP_MANAGER".equals(resolvedRole) && isOwner) {
                shopService.assignManagerForOwner(resolvedShop.getId(), newUser.getId(), actor);
            }

            auditLogService.log(admin.getUsername(), "CREATE_USER", "User",
                    String.valueOf(newUser.getId()), "Created user: " + username + " with role " + resolvedRole);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + username + "' created successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (com.inventory.exception.DuplicateEmailException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("editUser", userService.getById(id));
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/user-edit";
    }

    @PostMapping("/users/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String username,
                             @RequestParam String roleName,
                             @AuthenticationPrincipal UserDetails admin,
                             RedirectAttributes redirectAttributes) {
        userService.updateUser(id, username, roleName);
        auditLogService.log(admin.getUsername(), "UPDATE_USER", "User", String.valueOf(id),
                "Username set to '" + username + "', role to '" + roleName + "'");
        redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String toggleUserActive(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails admin,
                                   RedirectAttributes redirectAttributes) {
        var user = userService.toggleActive(id);
        String status = user.isActive() ? "activated" : "deactivated";
        auditLogService.log(admin.getUsername(), "TOGGLE_USER", "User", String.valueOf(id),
                "Account " + status);
        redirectAttributes.addFlashAttribute("successMessage",
                "User account has been " + status + ".");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                @AuthenticationPrincipal UserDetails admin,
                                RedirectAttributes redirectAttributes) {
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password must be at least 6 characters.");
            return "redirect:/admin/users/" + id + "/edit";
        }
        userService.resetPassword(id, newPassword);
        auditLogService.log(admin.getUsername(), "RESET_PASSWORD", "User", String.valueOf(id), "Password reset by admin");
        redirectAttributes.addFlashAttribute("successMessage", "Password reset successfully.");
        return "redirect:/admin/users";
    }

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String auditLogs(@AuthenticationPrincipal UserDetails admin, Model model) {
        User owner = userService.getByEmail(admin.getUsername());
        if (owner.getOrganization() != null) {
            model.addAttribute("logs", auditLogService.getRecentLogsForOrganization(owner.getOrganization().getId()));
        } else {
            model.addAttribute("logs", java.util.List.of());
        }
        return "admin/logs";
    }
}
