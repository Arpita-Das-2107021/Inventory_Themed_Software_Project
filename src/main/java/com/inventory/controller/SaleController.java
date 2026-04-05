// Define the package for this class.
package com.inventory.controller;

import com.inventory.dto.SaleRequest;
import com.inventory.dto.SaleItemRequest;
import com.inventory.model.Sale;
import com.inventory.model.User;
import com.inventory.service.AuditLogService;
import com.inventory.service.SaleService;
import com.inventory.service.UserService;
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

import java.util.List;
@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
// Define a public class.
public class SaleController {
    private final SaleService saleService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER','EMPLOYEE')")
    public String listSales(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User actor = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        List<Sale> sales = saleService.getSalesForActor(actor);
        model.addAttribute("sales", sales);
        // Return a value from this method.
        return "sales/list";
    // Close the current code block.
    }
    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','EMPLOYEE')")
    public String newSaleForm(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(value = "productId", required = false) Long productId,
                              Model model) {
        User actor = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        SaleRequest sr = new SaleRequest();
        SaleItemRequest item = new SaleItemRequest();
        item.setQuantity(1);
        // Check a condition before running code.
        if (productId != null) {
            item.setProductId(productId);
        // Close the current code block.
        }
        sr.setItems(java.util.List.of(item));

        model.addAttribute("saleRequest", sr);
        model.addAttribute("selectedProductId", productId);
        model.addAttribute("products", saleService.getSellableProducts(actor));
        // Return a value from this method.
        return "sales/new";
    // Close the current code block.
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('SHOP_MANAGER','EMPLOYEE')")
    public String createSale(
            @Valid @ModelAttribute("saleRequest") SaleRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        User seller = userService.findByEmail(userDetails.getUsername())
                .orElseThrow();

        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            model.addAttribute("products", saleService.getSellableProducts(seller));
            // Return a value from this method.
            return "sales/new";
        // Close the current code block.
        }

        try {
            Sale sale = saleService.createSale(request, seller);
            // Set a configuration key and value.
            String saleShopName = sale.getShop() != null ? sale.getShop().getName() : "N/A";
            auditLogService.log(userDetails.getUsername(), "CREATE_SALE", "Sale",
                    String.valueOf(sale.getId()), "Invoice generated for sale #" + sale.getId() + " at shop " + saleShopName);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Sale #" + sale.getId() + " created successfully. Invoice generated.");
            // Return a value from this method.
            return "redirect:/sales/" + sale.getId() + "/invoice";
        } catch (com.inventory.exception.InsufficientStockException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", saleService.getSellableProducts(seller));
            // Return a value from this method.
            return "sales/new";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("products", saleService.getSellableProducts(seller));
            // Return a value from this method.
            return "sales/new";
        // Close the current code block.
        }
    // Close the current code block.
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER','EMPLOYEE')")
    public String saleDetail(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User actor = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("sale", saleService.getSaleByIdForActor(id, actor));
        // Return a value from this method.
        return "sales/detail";
    // Close the current code block.
    }
    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ORGANIZATION_ADMIN','SHOP_MANAGER','EMPLOYEE')")
    public String saleInvoice(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        User actor = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("sale", saleService.getSaleByIdForActor(id, actor));
        // Return a value from this method.
        return "sales/detail";
    // Close the current code block.
    }
// Close the current code block.
}
