// Define the package where this class belongs
package com.inventory.controller;

// Import Category model (used as data object)
import com.inventory.model.Category;

// Import service for logging actions (create, update, delete)
import com.inventory.service.AuditLogService;

// Import service that handles category business logic
import com.inventory.service.CategoryService;

// Used for validation (e.g., checking empty fields)
import jakarta.validation.Valid;

// Lombok annotation to auto-generate constructor
import lombok.RequiredArgsConstructor;

// Security annotation to restrict access by roles
import org.springframework.security.access.prepost.PreAuthorize;

// Gets the currently logged-in user
import org.springframework.security.core.annotation.AuthenticationPrincipal;

// Spring Security user details object
import org.springframework.security.core.userdetails.UserDetails;

// Marks this class as a Spring MVC controller
import org.springframework.stereotype.Controller;

// Used to pass data to frontend (views)
import org.springframework.ui.Model;

// Holds validation errors
import org.springframework.validation.BindingResult;

// Used for mapping HTTP requests (GET, POST, etc.)
import org.springframework.web.bind.annotation.*;

// Used to send temporary messages after redirect
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Marks this class as a controller
@Controller

// Base URL for this controller → /categories
@RequestMapping("/categories")

// Only allow these roles to access this controller
@PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")

// Automatically creates constructor for final fields
@RequiredArgsConstructor

// Controller class for Category
public class CategoryController {

    // Service to handle category operations (CRUD)
    private final CategoryService categoryService;

    // Service to log user actions
    private final AuditLogService auditLogService;

    // ===================== GET ALL CATEGORIES =====================
    @GetMapping
    public String list(Model model) {

        // Get all categories and send to view
        model.addAttribute("categories", categoryService.getAllCategories());

        // Return the view page (templates/categories/list.html)
        return "categories/list";
    }

    // ===================== SHOW CREATE FORM =====================
    @GetMapping("/new")
    public String newForm(Model model) {

        // Send empty category object to form
        model.addAttribute("category", new Category());

        // Set form action URL
        model.addAttribute("formAction", "/categories");

        // Set form method (POST)
        model.addAttribute("formMethod", "post");

        // Return form view
        return "categories/form";

        
    }

    // ===================== CREATE CATEGORY =====================
    @PostMapping
    public String create(
            @Valid @ModelAttribute("category") Category category, // form data
            BindingResult bindingResult, // validation errors
            @AuthenticationPrincipal UserDetails user, // current user
            RedirectAttributes redirectAttributes, // flash messages
            Model model // view data
    ) {

        // If validation fails
        if (bindingResult.hasErrors()) {

            // Reload form with same settings
            model.addAttribute("formAction", "/categories");
            model.addAttribute("formMethod", "post");

            // Return form again with errors
            return "categories/form";
        }

        // Save category to database
        Category saved = categoryService.create(category);

        // Log the action
        auditLogService.log(
                user.getUsername(),
                "CREATE_CATEGORY",
                "Category",
                String.valueOf(saved.getId()),
                "Created: " + saved.getName()
        );

        // Show success message after redirect
        redirectAttributes.addFlashAttribute("successMessage", "Category created.");

        // Redirect to category list page
        return "redirect:/categories";
    }

    // ===================== SHOW EDIT FORM =====================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {

        // Get category by id and send to form
        model.addAttribute("category", categoryService.getById(id));

        // Set form action URL (update)
        model.addAttribute("formAction", "/categories/" + id);

        // Set method as PUT
        model.addAttribute("formMethod", "put");

        // Return form view
        return "categories/form";
    }

    // ===================== UPDATE CATEGORY =====================
    @PutMapping("/{id}")
    public String update(
            @PathVariable Long id, // category id
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes,
            Model model
    ) {

        // If validation fails
        if (bindingResult.hasErrors()) {

            // Reload form with correct settings
            model.addAttribute("formAction", "/categories/" + id);
            model.addAttribute("formMethod", "put");

            return "categories/form";
        }

        // Update category
        categoryService.update(id, category);

        // Log update action
        auditLogService.log(
                user.getUsername(),
                "UPDATE_CATEGORY",
                "Category",
                String.valueOf(id),
                "Updated: " + category.getName()
        );

        // Success message
        redirectAttributes.addFlashAttribute("successMessage", "Category updated.");

        // Redirect to list
        return "redirect:/categories";
    }

    // ===================== DELETE CATEGORY =====================
    @DeleteMapping("/{id}")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes
    ) {

        // Delete category
        categoryService.delete(id);

        // Log delete action
        auditLogService.log(
                user.getUsername(),
                "DELETE_CATEGORY",
                "Category",
                String.valueOf(id),
                "Deleted"
        );

        // Success message
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted.");

        // Redirect to list page
        return "redirect:/categories";
    }
}