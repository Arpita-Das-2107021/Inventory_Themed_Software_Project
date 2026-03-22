// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.Sale;
import com.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Define an interface.
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findBySeller(User seller);
    List<Sale> findByShopId(Long shopId);
    List<Sale> findByShopOrganizationId(Long organizationId);

    List<Sale> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Sale> findByShopIdAndCreatedAtBetween(Long shopId, LocalDateTime start, LocalDateTime end);
    List<Sale> findByShopOrganizationIdAndCreatedAtBetween(Long organizationId, LocalDateTime start, LocalDateTime end);
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenueBetween(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.id = :shopId AND s.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenueForShopBetween(@Param("shopId") Long shopId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.organization.id = :organizationId AND s.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenueForOrganizationBetween(@Param("organizationId") Long organizationId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);
    @Query("SELECT s.seller, COUNT(s), SUM(s.totalAmount) FROM Sale s GROUP BY s.seller")
    List<Object[]> getSalesCountAndRevenuePerSeller();
    @Query("SELECT s.seller, COUNT(s), SUM(s.totalAmount) FROM Sale s WHERE s.shop.id = :shopId GROUP BY s.seller")
    List<Object[]> getSalesCountAndRevenuePerSellerForShop(@Param("shopId") Long shopId);
    @Query("SELECT s.seller, COUNT(s), SUM(s.totalAmount) FROM Sale s WHERE s.shop.organization.id = :organizationId GROUP BY s.seller")
    List<Object[]> getSalesCountAndRevenuePerSellerForOrganization(@Param("organizationId") Long organizationId);
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.createdAt >= :startOfDay")
    long countSalesToday(@Param("startOfDay") LocalDateTime startOfDay);
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.shop.id = :shopId AND s.createdAt >= :startOfDay")
    long countSalesTodayForShop(@Param("shopId") Long shopId, @Param("startOfDay") LocalDateTime startOfDay);
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.shop.organization.id = :organizationId AND s.createdAt >= :startOfDay")
    long countSalesTodayForOrganization(@Param("organizationId") Long organizationId, @Param("startOfDay") LocalDateTime startOfDay);
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.createdAt >= :startOfDay")
    BigDecimal getTotalRevenueToday(@Param("startOfDay") LocalDateTime startOfDay);
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.id = :shopId AND s.createdAt >= :startOfDay")
    BigDecimal getTotalRevenueTodayForShop(@Param("shopId") Long shopId, @Param("startOfDay") LocalDateTime startOfDay);
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.shop.organization.id = :organizationId AND s.createdAt >= :startOfDay")
    BigDecimal getTotalRevenueTodayForOrganization(@Param("organizationId") Long organizationId, @Param("startOfDay") LocalDateTime startOfDay);
// Close the current code block.
}
