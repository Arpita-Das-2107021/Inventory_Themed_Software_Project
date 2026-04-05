// Define the package for this class.
package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.model.Role;
import com.inventory.model.Sale;
import com.inventory.model.User;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
// Define a public class.
public class ReportService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    @Transactional(readOnly = true)
    public List<Sale> getSalesReport(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        // Return a value from this method.
        return saleRepository.findByCreatedAtBetween(start, end);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Sale> getSalesReportForUser(LocalDate from, LocalDate to, User actor) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            // Return a value from this method.
            return saleRepository.findByShopIdAndCreatedAtBetween(actor.getShop().getId(), start, end);
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            // Return a value from this method.
            return saleRepository.findByShopOrganizationIdAndCreatedAtBetween(actor.getOrganization().getId(), start, end);
        // Close the current code block.
        }
        // Return a value from this method.
        return List.of();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> getInventoryReport() {
        // Return a value from this method.
        return productRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> getInventoryReportForUser(User actor) {
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            // Return a value from this method.
            return productRepository.findByShopIdAndActiveTrue(actor.getShop().getId());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            // Return a value from this method.
            return productRepository.findByShopOrganizationIdAndActiveTrue(actor.getOrganization().getId());
        // Close the current code block.
        }
        // Return a value from this method.
        return List.of();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Object[]> getSellerActivityReport() {
        // Return a value from this method.
        return saleRepository.getSalesCountAndRevenuePerSeller();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Object[]> getSellerActivityReportForUser(User actor) {
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            // Return a value from this method.
            return saleRepository.getSalesCountAndRevenuePerSellerForShop(actor.getShop().getId());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            // Return a value from this method.
            return saleRepository.getSalesCountAndRevenuePerSellerForOrganization(actor.getOrganization().getId());
        // Close the current code block.
        }
        // Return a value from this method.
        return List.of();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueBetween(LocalDate from, LocalDate to) {
        BigDecimal result = saleRepository.getTotalRevenueBetween(
                from.atStartOfDay(), to.atTime(LocalTime.MAX));
        // Return a value from this method.
        return result != null ? result : BigDecimal.ZERO;
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueBetweenForUser(LocalDate from, LocalDate to, User actor) {
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            // Return a value from this method.
            return saleRepository.getTotalRevenueForShopBetween(actor.getShop().getId(), from.atStartOfDay(), to.atTime(LocalTime.MAX));
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            // Return a value from this method.
            return saleRepository.getTotalRevenueForOrganizationBetween(actor.getOrganization().getId(), from.atStartOfDay(), to.atTime(LocalTime.MAX));
        // Close the current code block.
        }
        // Return a value from this method.
        return BigDecimal.ZERO;
    // Close the current code block.
    }
// Close the current code block.
}
