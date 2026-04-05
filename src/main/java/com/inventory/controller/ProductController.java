// Define the package for this class.
package com.inventory.controller;

import com.inventory.dto.StockAdjustmentRequest;
import com.inventory.model.Product;
import com.inventory.model.User;
import com.inventory.service.AuditLogService;
import com.inventory.service.CategoryService;
import com.inventory.service.ProductService;
import com.inventory.service.ShopService;
import com.inventory.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;
@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
// Define a public class.
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ShopService shopService;
    private final AuditLogService auditLogService;
    private final UserService userService;
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String listProducts(@RequestParam(required = false) String search,
                               @AuthenticationPrincipal UserDetails user,
                               Model model) {
        User actor = userService.findByEmail(user.getUsername()).orElseThrow();
        List<Product> products;
        // Check a condition before running code.
        if (search != null && !search.isBlank()) {
            products = productService.searchManageableProducts(actor, search);
            model.addAttribute("search", search);
        } else {
            products = productService.getManageableProducts(actor);
        // Close the current code block.
        }

        model.addAttribute("products", sortProductsByShop(products));
        // Return a value from this method.
        return "products/list";
    // Close the current code block.
    }
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String newProductForm(@AuthenticationPrincipal UserDetails user, Model model) {
        User actor = userService.findByEmail(user.getUsername()).orElseThrow();
        model.addAttribute("product", new Product());
        populateFormContext(model, actor, null, "/products", "post");
        // Return a value from this method.
        return "products/form";
    // Close the current code block.
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes,
            Model model) {

        User actor = userService.findByEmail(user.getUsername()).orElseThrow();

        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            populateFormContext(model, actor, shopId, "/products", "post");
            // Return a value from this method.
            return "products/form";
        // Close the current code block.
        }
        // Check a condition before running code.
        if (categoryId != null) product.setCategory(categoryService.getById(categoryId));

        try {
            Product saved = productService.createProduct(product, actor, shopId);
            auditLogService.log(user.getUsername(), "CREATE_PRODUCT", "Product",
                    // Set a configuration key and value.
                    String.valueOf(saved.getId()), "Created: " + saved.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully.");
            // Return a value from this method.
            return "redirect:/products";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateFormContext(model, actor, shopId, "/products", "post");
            // Return a value from this method.
            return "products/form";
        // Close the current code block.
        }
    // Close the current code block.
    }
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String editProductForm(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails user,
                                  Model model) {
        User actor = userService.findByEmail(user.getUsername()).orElseThrow();
        Product existing = productService.getProductByIdForActor(id, actor);
        model.addAttribute("product", existing);
        // Set a configuration key and value.
        populateFormContext(model, actor, existing.getShop() != null ? existing.getShop().getId() : null,
                "/products/" + id, "put");
        // Return a value from this method.
        return "products/form";
    // Close the current code block.
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long shopId,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes,
            Model model) {

        User actor = userService.findByEmail(user.getUsername()).orElseThrow();

        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            populateFormContext(model, actor, shopId, "/products/" + id, "put");
            // Return a value from this method.
            return "products/form";
        // Close the current code block.
        }
        // Check a condition before running code.
        if (categoryId != null) product.setCategory(categoryService.getById(categoryId));

        productService.updateProductForActor(id, product, actor);
        auditLogService.log(user.getUsername(), "UPDATE_PRODUCT", "Product",
                // Set a configuration key and value.
                String.valueOf(id), "Updated: " + product.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully.");
        // Return a value from this method.
        return "redirect:/products";
    // Close the current code block.
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String deleteProduct(@PathVariable Long id,
                                @AuthenticationPrincipal UserDetails user,
                                RedirectAttributes redirectAttributes) {
        User actor = userService.findByEmail(user.getUsername()).orElseThrow();
        String name = productService.getProductByIdForActor(id, actor).getName();
        productService.deleteProductForActor(id, actor);
        auditLogService.log(user.getUsername(), "DELETE_PRODUCT", "Product",
                // Set a configuration key and value.
                String.valueOf(id), "Deleted: " + name);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted.");
        // Return a value from this method.
        return "redirect:/products";
    // Close the current code block.
    }
    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String stockAdjustmentForm(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails user,
                                      Model model) {
        User actor = userService.findByEmail(user.getUsername()).orElseThrow();
        model.addAttribute("product", productService.getProductByIdForActor(id, actor));
        model.addAttribute("stockAdjustmentRequest", new StockAdjustmentRequest());
        model.addAttribute("history", productService.getStockHistory(id));
        // Return a value from this method.
        return "products/stock-form";
    // Close the current code block.
    }
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('EMPLOYEE','SHOP_MANAGER','ORGANIZATION_ADMIN')")
    public String adjustStock(
            @PathVariable Long id,
            @Valid @ModelAttribute("stockAdjustmentRequest") StockAdjustmentRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails user,
            RedirectAttributes redirectAttributes,
            Model model) {

        User actor = userService.findByEmail(user.getUsername()).orElseThrow();

        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", productService.getProductByIdForActor(id, actor));
            model.addAttribute("history", productService.getStockHistory(id));
            // Return a value from this method.
            return "products/stock-form";
        // Close the current code block.
        }

        int delta = request.getType().equals("DECREASE") || request.getType().equals("SALE")
                // Set a configuration key and value.
                ? -request.getQuantity() : request.getQuantity();
        productService.adjustStockForActor(id, delta, request.getType(), request.getReason(), actor);
        auditLogService.log(user.getUsername(), "STOCK_ADJUST", "Product",
                // Set a configuration key and value.
                String.valueOf(id), "Type: " + request.getType() + ", Qty: " + request.getQuantity());
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully.");
        // Return a value from this method.
        return "redirect:/products";
    // Close the current code block.
    }
    private void populateFormContext(Model model, User actor, Long selectedShopId, String formAction, String formMethod) {
        boolean isOwner = actor.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("selectedShopId", selectedShopId);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("shops", isOwner && actor.getOrganization() != null
                ? shopService.getShopsByOrganization(actor.getOrganization().getId())
                // Set a configuration key and value.
                : java.util.List.of());
        model.addAttribute("actorShop", actor.getShop());
        model.addAttribute("formAction", formAction);
        model.addAttribute("formMethod", formMethod);
    // Close the current code block.
    }

    private List<Product> sortProductsByShop(List<Product> products) {
        return products.stream()
                .sorted(Comparator
                        .comparing((Product p) -> p.getShop() != null && p.getShop().getName() != null
                                ? p.getShop().getName()
                                : "")
                        .thenComparing(p -> p.getName() != null ? p.getName() : ""))
                .toList();
    }
// Close the current code block.
}
