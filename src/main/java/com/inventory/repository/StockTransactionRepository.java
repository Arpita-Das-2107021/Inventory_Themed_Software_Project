// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.Product;
import com.inventory.model.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Define an interface.
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {
    List<StockTransaction> findByProductOrderByCreatedAtDesc(Product product);
// Close the current code block.
}
