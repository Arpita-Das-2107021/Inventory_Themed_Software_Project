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
public class OrganizationController {

    private final OrganizationService organizationService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var owner = userService.getByEmail(userDetails.getUsername());
        if (owner.getOrganization() != null) {
            model.addAttribute("organizations", java.util.List.of(owner.getOrganization()));
        } else {
            model.addAttribute("organizations", java.util.List.of());
        }
        return "admin/organizations";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("organization", new Organization());
        return "admin/organization-form";
    }

    @PostMapping
    public String create(@RequestParam String name,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        var owner = userService.getByEmail(userDetails.getUsername());
        if (owner.getOrganization() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Each owner can manage one organization in this showcase.");
            return "redirect:/organizations";
        }
        organizationService.create(name);
        redirectAttributes.addFlashAttribute("successMessage", "Organization created successfully.");
        return "redirect:/organizations";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        var owner = userService.getByEmail(userDetails.getUsername());
        if (owner.getOrganization() == null || !owner.getOrganization().getId().equals(id)) {
            throw new IllegalArgumentException("You can only edit your own organization.");
        }
        model.addAttribute("organization", owner.getOrganization());
        return "admin/organization-form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @RequestParam String name,
                         RedirectAttributes redirectAttributes) {
        organizationService.update(id, name);
        redirectAttributes.addFlashAttribute("successMessage", "Organization updated successfully.");
        return "redirect:/organizations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        organizationService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Organization deleted.");
        return "redirect:/organizations";
    }
}
