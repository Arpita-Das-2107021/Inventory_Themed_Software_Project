// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.Sale;
import com.inventory.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Define an interface.
public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
    List<SaleItem> findBySale(Sale sale);
// Close the current code block.
}
