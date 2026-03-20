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
public class ShopController {

    private final ShopService shopService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String list(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User actor = userService.getByEmail(userDetails.getUsername());
        actor = userService.ensureOwnerOrganization(actor);
        model.addAttribute("shops", shopService.getAccessibleShops(actor));
        return "admin/shops";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String newForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        model.addAttribute("shop", new Shop());
        return "admin/shop-form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String create(@RequestParam String name,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.createForOwner(name, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Shop created successfully.");
        return "redirect:/shops";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        Shop shop = shopService.getById(id);
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
            !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            throw new IllegalArgumentException("You can only edit shops in your own organization.");
        }
        model.addAttribute("shop", shopService.getById(id));
        model.addAttribute("users", userService.getUsersByOrganization(owner.getOrganization().getId()));
        return "admin/shop-form";
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
        return "redirect:/shops";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.deleteForOwner(id, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Shop deleted.");
        return "redirect:/shops";
    }

    @PostMapping("/{shopId}/assign-manager/{userId}")
    @PreAuthorize("hasRole('ORGANIZATION_ADMIN')")
    public String assignManager(@PathVariable Long shopId, @PathVariable Long userId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User owner = userService.ensureOwnerOrganization(userService.getByEmail(userDetails.getUsername()));
        shopService.assignManagerForOwner(shopId, userId, owner);
        redirectAttributes.addFlashAttribute("successMessage", "Manager assigned successfully.");
        return "redirect:/shops/" + shopId + "/edit";
    }

    @PostMapping("/{shopId}/assign-employee/{userId}")
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String assignEmployee(@PathVariable Long shopId, @PathVariable Long userId,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User actor = userService.getByEmail(userDetails.getUsername());
        boolean owner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        if (owner) {
            shopService.assignEmployeeForOwner(shopId, userId, actor);
        } else {
            shopService.assignEmployeeForManager(shopId, userId, actor);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Employee assigned successfully.");
        return "redirect:/shops/" + shopId + "/edit";
    }

    @GetMapping("/{id}/employees")
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String listEmployees(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User actor = userService.getByEmail(userDetails.getUsername());
        Shop shop = shopService.getById(id);
        boolean owner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        if (owner) {
            actor = userService.ensureOwnerOrganization(actor);
            if (actor.getOrganization() == null || shop.getOrganization() == null ||
                    !actor.getOrganization().getId().equals(shop.getOrganization().getId())) {
                throw new IllegalArgumentException("You can only view employees for shops in your organization.");
            }
        } else if (actor.getShop() == null || !actor.getShop().getId().equals(id)) {
            throw new IllegalArgumentException("Shop admins can only view employees for their own shop.");
        }
        model.addAttribute("shop", shop);
        model.addAttribute("employees", shopService.getEmployeesForShop(id));
        return "admin/shop-employees";
    }
}
