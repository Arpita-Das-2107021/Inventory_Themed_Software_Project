// Define the package for this class.
package com.inventory.service;

import com.inventory.dto.SaleItemRequest;
import com.inventory.dto.SaleRequest;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.*;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
// Define a public class.
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;
    @Transactional(readOnly = true)
    public List<Sale> getSalesForActor(User actor) {
        Set<String> roles = roleNames(actor);
        // Check a condition before running code.
        if (roles.contains("ROLE_ORGANIZATION_ADMIN")) {
            // Check a condition before running code.
            if (actor.getOrganization() == null) {
                // Return a value from this method.
                return List.of();
            // Close the current code block.
            }
            // Return a value from this method.
            return saleRepository.findByShopOrganizationId(actor.getOrganization().getId());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roles.contains("ROLE_SHOP_MANAGER") || roles.contains("ROLE_EMPLOYEE")) {
            // Check a condition before running code.
            if (actor.getShop() == null) {
                // Return a value from this method.
                return List.of();
            // Close the current code block.
            }
            // Return a value from this method.
            return saleRepository.findByShopId(actor.getShop().getId());
        // Close the current code block.
        }
        // Return a value from this method.
        return List.of();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> getSellableProducts(User actor) {
        Set<String> roles = roleNames(actor);
        // Check a condition before running code.
        if (roles.contains("ROLE_ORGANIZATION_ADMIN")) {
            // Check a condition before running code.
            if (actor.getOrganization() == null) {
                // Return a value from this method.
                return List.of();
            // Close the current code block.
            }
            // Return a value from this method.
            return productRepository.findByShopOrganizationIdAndActiveTrue(actor.getOrganization().getId());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roles.contains("ROLE_SHOP_MANAGER") || roles.contains("ROLE_EMPLOYEE")) {
            // Check a condition before running code.
            if (actor.getShop() == null) {
                // Return a value from this method.
                return List.of();
            // Close the current code block.
            }
            // Return a value from this method.
            return productRepository.findByShopIdAndActiveTrue(actor.getShop().getId());
        // Close the current code block.
        }
        // Return a value from this method.
        return List.of();
    // Close the current code block.
    }
    @Transactional
    public Sale createSale(SaleRequest request, User seller) {
        // Check a condition before running code.
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("A sale must contain at least one item.");
        // Close the current code block.
        }

        Set<String> roles = roleNames(seller);
        boolean isShopStaff = roles.contains("ROLE_SHOP_MANAGER") || roles.contains("ROLE_EMPLOYEE");

        // Check a condition before running code.
        if (!isShopStaff) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Only shop staff can create sales.");
        // Close the current code block.
        }

        // Check a condition before running code.
        if (seller.getShop() == null) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Shop staff must be assigned to a shop before selling products.");
        // Close the current code block.
        }

        Sale sale = Sale.builder()
                .seller(seller)
            .buyerName(request.getBuyerName().trim())
                .totalAmount(BigDecimal.ZERO)
                .saleItems(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        Shop saleShop = null;

        // Start a loop over values.
        for (SaleItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductById(itemReq.getProductId());

            // Check a condition before running code.
            if (!product.isActive()) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("Product '" + product.getName() + "' is inactive and cannot be sold.");
            // Close the current code block.
            }
            // Check a condition before running code.
            if (product.getShop() == null) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("Product '" + product.getName() + "' is not assigned to a shop.");
            // Close the current code block.
            }

            // Check a condition before running code.
            if (!product.getShop().getId().equals(seller.getShop().getId())) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("You can only sell products from your assigned shop.");
            // Close the current code block.
            }

            // Check a condition before running code.
            if (saleShop == null) {
                saleShop = product.getShop();
            } else if (!saleShop.getId().equals(product.getShop().getId())) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("A single invoice can only include products from one shop.");
            // Close the current code block.
            }

            // Check a condition before running code.
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                // Throw an exception for an error case.
                throw new com.inventory.exception.InsufficientStockException(
                    "Insufficient stock for '" + product.getName() +
                    // Set a configuration key and value.
                    "'. Available: " + product.getStockQuantity() +
                    // Set a configuration key and value.
                    ", Requested: " + itemReq.getQuantity()
                );
            // Close the current code block.
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .price(unitPrice)
                    .subtotal(subtotal)
                    .build();

            sale.getSaleItems().add(saleItem);
            total = total.add(subtotal);
        // Close the current code block.
        }

        sale.setShop(saleShop);
        sale.setTotalAmount(total);
        Sale savedSale = saleRepository.save(sale);

        // Start a loop over values.
        for (SaleItem item : savedSale.getSaleItems()) {
            productService.adjustStock(item.getProduct().getId(), -item.getQuantity(), "SALE",
                    "Sold in sale #" + savedSale.getId());
        // Close the current code block.
        }

        // Return a value from this method.
        return savedSale;
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Sale getSaleById(Long id) {
        // Return a value from this method.
        return saleRepository.findById(id)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Sale getSaleByIdForActor(Long id, User actor) {
        Sale sale = getSaleById(id);
        // Check a condition before running code.
        if (canAccessSale(actor, sale)) {
            // Return a value from this method.
            return sale;
        // Close the current code block.
        }
        // Throw an exception for an error case.
        throw new IllegalArgumentException("You do not have permission to view this sale.");
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        // Return a value from this method.
        return saleRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Sale> getSalesBySeller(User seller) {
        // Return a value from this method.
        return saleRepository.findBySeller(seller);
    // Close the current code block.
    }
    private boolean canAccessSale(User actor, Sale sale) {
        Set<String> roles = roleNames(actor);

        // Check a condition before running code.
        if (roles.contains("ROLE_ORGANIZATION_ADMIN")) {
            // Return a value from this method.
            return actor.getOrganization() != null &&
                    sale.getShop() != null &&
                    sale.getShop().getOrganization() != null &&
                    actor.getOrganization().getId().equals(sale.getShop().getOrganization().getId());
        // Close the current code block.
        }

        // Check a condition before running code.
        if (roles.contains("ROLE_SHOP_MANAGER") || roles.contains("ROLE_EMPLOYEE")) {
            // Return a value from this method.
            return actor.getShop() != null &&
                    sale.getShop() != null &&
                    actor.getShop().getId().equals(sale.getShop().getId());
        // Close the current code block.
        }

        // Return a value from this method.
        return false;
    // Close the current code block.
    }
    private Set<String> roleNames(User user) {
        // Return a value from this method.
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    // Close the current code block.
    }
// Close the current code block.
}
