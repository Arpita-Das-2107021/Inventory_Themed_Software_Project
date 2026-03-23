// Define the package for this class.
package com.inventory.controller;

import com.inventory.model.Shop;
import com.inventory.model.User;
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
@RequestMapping("/shops")
@RequiredArgsConstructor
// Define a public class.
public class ShopController {
    private final ShopService shopService;
    private final UserService userService;
    @GetMapping
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User actor = userService.getByEmail(userDetails.getUsername());
        actor = userService.ensureOwnerOrganization(actor);
        model.addAttribute("shops", shopService.getAccessibleShops(actor));
        // Return a value from this method.
        return "admin/shops";
    // Close the current code block.
    }
    @GetMapping("/new")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String newForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        model.addAttribute("shop", new Shop());
        // Return a value from this method.
        return "admin/shop-form";
    // Close the current code block.
    }
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String create(@RequestParam String name,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.createForOwner(name, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Shop created successfully.");
        // Return a value from this method.
        return "redirect:/shops";
    // Close the current code block.
    }
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        Shop shop = shopService.getById(id);
        // Check a condition before running code.
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
            !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("You can only edit shops in your own organization.");
        // Close the current code block.
        }
        model.addAttribute("shop", shopService.getById(id));
        model.addAttribute("users", userService.getUsersByOrganization(owner.getOrganization().getId()));
        // Return a value from this method.
        return "admin/shop-form";
    // Close the current code block.
    }
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.updateForOwner(id, name, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Shop updated.");
        // Return a value from this method.
        return "redirect:/shops";
    // Close the current code block.
    }
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.deleteForOwner(id, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Shop deleted.");
        // Return a value from this method.
        return "redirect:/shops";
    // Close the current code block.
    }
    @PostMapping("/{shopId}/assign-manager/{userId}")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String assignManager(@PathVariable Long shopId, @PathVariable Long userId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.assignManagerForOwner(shopId, userId, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Manager assigned successfully.");
        // Return a value from this method.
        return "redirect:/shops/" + shopId + "/edit";
    // Close the current code block.
    }
    @PostMapping("/{shopId}/assign-employee/{userId}")
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String assignEmployee(@PathVariable Long shopId, @PathVariable Long userId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User actor = userService.getByEmail(userDetails.getUsername());
        boolean owner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        // Check a condition before running code.
        if (owner) {
            shopService.assignEmployeeForOwner(shopId, userId, actor);
        } else {
            shopService.assignEmployeeForManager(shopId, userId, actor);
        // Close the current code block.
        }
        redirectAttributes.addFlashAttribute("successMessage", "Employee assigned successfully.");
        // Return a value from this method.
        return "redirect:/shops/" + shopId + "/edit";
    // Close the current code block.
    }
    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String listEmployees(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User actor = userService.getByEmail(userDetails.getUsername());
        Shop shop = shopService.getById(id);
        boolean owner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        // Check a condition before running code.
        if (owner) {
            actor = userService.ensureOwnerOrganization(actor);
            // Check a condition before running code.
            if (actor.getOrganization() == null || shop.getOrganization() == null ||
                    !actor.getOrganization().getId().equals(shop.getOrganization().getId())) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("You can only view employees for shops in your organization.");
            // Close the current code block.
            }
        } else if (actor.getShop() == null || !actor.getShop().getId().equals(id)) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Shop admins can only view employees for their own shop.");
        // Close the current code block.
        }
        model.addAttribute("shop", shop);
        model.addAttribute("employees", shopService.getEmployeesForShop(id));
        // Return a value from this method.
        return "admin/shop-employees";
    // Close the current code block.
    }
// Close the current code block.
}
