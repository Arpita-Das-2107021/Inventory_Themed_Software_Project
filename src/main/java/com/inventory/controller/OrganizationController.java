// Define the package for this class.
package com.inventory.controller;

import com.inventory.model.Organization;
import com.inventory.service.OrganizationService;
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
@RequestMapping("/organizations")
@PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
@RequiredArgsConstructor
// Define a public class.
public class OrganizationController {
    private final OrganizationService organizationService;
    private final UserService userService;
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var owner = userService.getByEmail(userDetails.getUsername());
        // Check a condition before running code.
        if (owner.getOrganization() != null) {
            model.addAttribute("organizations", java.util.List.of(owner.getOrganization()));
        } else {
            model.addAttribute("organizations", java.util.List.of());
        // Close the current code block.
        }
        // Return a value from this method.
        return "admin/organizations";
    // Close the current code block.
    }
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("organization", new Organization());
        // Return a value from this method.
        return "admin/organization-form";
    // Close the current code block.
    }
    @PostMapping
    public String create(@RequestParam String name,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        var owner = userService.getByEmail(userDetails.getUsername());
        // Check a condition before running code.
        if (owner.getOrganization() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Each owner can manage one organization in this showcase.");
            // Return a value from this method.
            return "redirect:/organizations";
        // Close the current code block.
        }
        organizationService.create(name);
        redirectAttributes.addFlashAttribute("successMessage", "Organization created successfully.");
        // Return a value from this method.
        return "redirect:/organizations";
    // Close the current code block.
    }
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        var owner = userService.getByEmail(userDetails.getUsername());
        // Check a condition before running code.
        if (owner.getOrganization() == null || !owner.getOrganization().getId().equals(id)) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("You can only edit your own organization.");
        // Close the current code block.
        }
        model.addAttribute("organization", owner.getOrganization());
        // Return a value from this method.
        return "admin/organization-form";
    // Close the current code block.
    }
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @RequestParam String name,
                         RedirectAttributes redirectAttributes) {
        organizationService.update(id, name);
        redirectAttributes.addFlashAttribute("successMessage", "Organization updated successfully.");
        // Return a value from this method.
        return "redirect:/organizations";
    // Close the current code block.
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        organizationService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Organization deleted.");
        // Return a value from this method.
        return "redirect:/organizations";
    // Close the current code block.
    }
// Close the current code block.
}
