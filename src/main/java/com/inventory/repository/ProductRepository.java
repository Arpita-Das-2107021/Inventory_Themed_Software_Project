// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.Category;
import com.inventory.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// Define an interface.
public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < p.lowStockThreshold AND p.active = true")
    List<Product> findLowStockProducts();

    List<Product> findByActiveTrue();
    List<Product> findByShopIdAndActiveTrue(Long shopId);
    List<Product> findByShopOrganizationIdAndActiveTrue(Long organizationId);

    List<Product> findByCategoryAndActiveTrue(Category category);
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           // Set a configuration key and value.
           "(LOWER(p.name) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
           // Set a configuration key and value.
           "LOWER(p.description) LIKE LOWER(CONCAT('%',:query,'%')))")
    List<Product> searchActiveProducts(@Param("query") String query);

    long countByActiveTrue();
// Close the current code block.
}
