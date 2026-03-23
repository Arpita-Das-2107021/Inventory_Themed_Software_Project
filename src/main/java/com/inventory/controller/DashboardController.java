// Define the package for this class.
package com.inventory.controller;

import com.inventory.service.AuditLogService;
import com.inventory.service.DashboardService;
import com.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
@RequiredArgsConstructor
// Define a public class.
public class DashboardController {
    private final DashboardService dashboardService;
    private final AuditLogService auditLogService;
    private final UserService userService;
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, Authentication authentication) {
        // Check a condition before running code.
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            // Return a value from this method.
            return "redirect:/auth/login";
        // Close the current code block.
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZATION_ADMIN"));
        boolean isEmployee = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        // Check a condition before running code.
        if (isEmployee) {
            // Return a value from this method.
            return "redirect:/sales";
        // Close the current code block.
        }

        var actor = userService.getByEmail(authentication.getName());
        DashboardService.DashboardMetrics metrics = dashboardService.getMetricsForUser(actor);
        model.addAttribute("metrics", metrics);

        // Check a condition before running code.
        if (isAdmin && actor.getOrganization() != null) {
            model.addAttribute("recentLogs", auditLogService.getRecentLogsForOrganization(actor.getOrganization().getId())
                .stream().limit(10).toList());
        // Close the current code block.
        }

        boolean isShopAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SHOP_MANAGER"));
        // Check a condition before running code.
        if (isShopAdmin && actor.getShop() != null) {
            model.addAttribute("recentLogs", auditLogService.getRecentLogsForShop(actor.getShop().getId())
                .stream().limit(10).toList());
        // Close the current code block.
        }
        // Return a value from this method.
        return "dashboard";
    // Close the current code block.
    }
// Close the current code block.
}

