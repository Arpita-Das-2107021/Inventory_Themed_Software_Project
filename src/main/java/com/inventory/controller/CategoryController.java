// Define the package for this class.
package com.inventory.controller;

import com.inventory.model.Category;
import com.inventory.service.AuditLogService;
import com.inventory.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/categories")
@PreAuthorize("hasAnyRole('SHOP_MANAGER','ORGANIZATION_ADMIN')")
@RequiredArgsConstructor
// Define a public class.
public class CategoryController {
    private final CategoryService categoryService;
    private final AuditLogService auditLogService;
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        // Return a value from this method.
        return "categories/list";
    // Close the current code block.
    }
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("formAction", "/categories");
        model.addAttribute("formMethod", "post");
        // Return a value from this method.
        return "categories/form";
    // Close the current code block.
    }
    @PostMapping
    public String create(@Valid @ModelAttribute("category") Category category,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/categories");
            model.addAttribute("formMethod", "post");
            // Return a value from this method.
            return "categories/form";
        // Close the current code block.
        }
        Category saved = categoryService.create(category);
        auditLogService.log(user.getUsername(), "CREATE_CATEGORY", "Category",
                // Set a configuration key and value.
                String.valueOf(saved.getId()), "Created: " + saved.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Category created.");
        // Return a value from this method.
        return "redirect:/categories";
    // Close the current code block.
    }
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getById(id));
        model.addAttribute("formAction", "/categories/" + id);
        model.addAttribute("formMethod", "put");
        // Return a value from this method.
        return "categories/form";
    // Close the current code block.
    }
    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("category") Category category,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/categories/" + id);
            model.addAttribute("formMethod", "put");
            // Return a value from this method.
            return "categories/form";
        // Close the current code block.
        }
        categoryService.update(id, category);
        auditLogService.log(user.getUsername(), "UPDATE_CATEGORY", "Category",
                // Set a configuration key and value.
                String.valueOf(id), "Updated: " + category.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Category updated.");
        // Return a value from this method.
        return "redirect:/categories";
    // Close the current code block.
    }
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails user,
                         RedirectAttributes redirectAttributes) {
        categoryService.delete(id);
        auditLogService.log(user.getUsername(), "DELETE_CATEGORY", "Category",
                String.valueOf(id), "Deleted");
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted.");
        // Return a value from this method.
        return "redirect:/categories";
    // Close the current code block.
    }
// Close the current code block.
}
