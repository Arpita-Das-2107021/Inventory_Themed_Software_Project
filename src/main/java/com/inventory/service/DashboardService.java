// Define the package for this class.
package com.inventory.service;

import com.inventory.model.Product;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import com.inventory.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
// Define a public class.
public class DashboardService {
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public DashboardMetrics getMetrics() {
        long totalProducts = productRepository.countByActiveTrue();
        long totalSales = saleRepository.count();
        BigDecimal totalRevenue = saleRepository.getTotalRevenueBetween(
                LocalDate.of(2000, 1, 1).atStartOfDay(),
                LocalDateTime.now());
        // Check a condition before running code.
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        List<Product> lowStockProducts = productRepository.findLowStockProducts();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long salesToday = saleRepository.countSalesToday(startOfToday);
        BigDecimal revenueToday = saleRepository.getTotalRevenueToday(startOfToday);
        // Check a condition before running code.
        if (revenueToday == null) revenueToday = BigDecimal.ZERO;

        long totalUsers = userRepository.count();

        // Return a value from this method.
        return new DashboardMetrics(totalProducts, totalSales, totalRevenue,
                lowStockProducts.size(), lowStockProducts, salesToday, revenueToday, totalUsers);
    // Close the current code block.
    }
        @Transactional(readOnly = true)
        public DashboardMetrics getMetricsForUser(User actor) {
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            long totalProducts = productRepository.findByShopIdAndActiveTrue(actor.getShop().getId()).size();
            long totalSales = saleRepository.findByShopId(actor.getShop().getId()).size();
            BigDecimal totalRevenue = saleRepository.getTotalRevenueForShopBetween(
                actor.getShop().getId(), LocalDate.of(2000, 1, 1).atStartOfDay(), LocalDateTime.now());
            List<Product> lowStockProducts = productRepository.findByShopIdAndActiveTrue(actor.getShop().getId())
                // Set a configuration key and value.
                .stream().filter(Product::isLowStock).toList();
            long salesToday = saleRepository.countSalesTodayForShop(actor.getShop().getId(), startOfToday);
            BigDecimal revenueToday = saleRepository.getTotalRevenueTodayForShop(actor.getShop().getId(), startOfToday);
            // Return a value from this method.
            return new DashboardMetrics(totalProducts, totalSales, totalRevenue,
                lowStockProducts.size(), lowStockProducts, salesToday, revenueToday, 0);
        // Close the current code block.
        }

        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            long totalProducts = productRepository.findByShopOrganizationIdAndActiveTrue(actor.getOrganization().getId()).size();
            long totalSales = saleRepository.findByShopOrganizationId(actor.getOrganization().getId()).size();
            BigDecimal totalRevenue = saleRepository.getTotalRevenueForOrganizationBetween(
                actor.getOrganization().getId(), LocalDate.of(2000, 1, 1).atStartOfDay(), LocalDateTime.now());
            List<Product> lowStockProducts = productRepository.findByShopOrganizationIdAndActiveTrue(actor.getOrganization().getId())
                // Set a configuration key and value.
                .stream().filter(Product::isLowStock).toList();
            long salesToday = saleRepository.countSalesTodayForOrganization(actor.getOrganization().getId(), startOfToday);
            BigDecimal revenueToday = saleRepository.getTotalRevenueTodayForOrganization(actor.getOrganization().getId(), startOfToday);
            long totalUsers = userRepository.countByOrganizationId(actor.getOrganization().getId());
            // Return a value from this method.
            return new DashboardMetrics(totalProducts, totalSales, totalRevenue,
                lowStockProducts.size(), lowStockProducts, salesToday, revenueToday, totalUsers);
        // Close the current code block.
        }

        // Return a value from this method.
        return getMetrics();
        // Close the current code block.
        }
    @Data
    public static class DashboardMetrics {
        private final long totalProducts;
        private final long totalSales;
        private final BigDecimal totalRevenue;
        private final int lowStockCount;
        private final List<Product> lowStockProducts;
        private final long salesToday;
        private final BigDecimal revenueToday;
        private final long totalUsers;
    // Close the current code block.
    }
// Close the current code block.
}
