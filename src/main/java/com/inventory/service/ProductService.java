// Define the package for this class.
package com.inventory.service;

import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Product;
import com.inventory.model.Role;
import com.inventory.model.StockTransaction;
import com.inventory.model.User;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.ShopRepository;
import com.inventory.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
// Define a public class.
public class ProductService {
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final StockTransactionRepository stockTransactionRepository;
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        // Return a value from this method.
        return productRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        // Return a value from this method.
        return productRepository.findByActiveTrue();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> getManageableProducts(User actor) {
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            // Return a value from this method.
            return productRepository.findByShopOrganizationIdAndActiveTrue(actor.getOrganization().getId());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            // Return a value from this method.
            return productRepository.findByShopIdAndActiveTrue(actor.getShop().getId());
        // Close the current code block.
        }
        // Return a value from this method.
        return productRepository.findByActiveTrue();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        // Check a condition before running code.
        if (query == null || query.isBlank()) return productRepository.findByActiveTrue();
        // Return a value from this method.
        return productRepository.searchActiveProducts(query);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> searchManageableProducts(User actor, String query) {
        // Check a condition before running code.
        if (query == null || query.isBlank()) {
            // Return a value from this method.
            return getManageableProducts(actor);
        // Close the current code block.
        }
        String needle = query.toLowerCase();
        // Return a value from this method.
        return getManageableProducts(actor).stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(needle)) ||
                             (p.getDescription() != null && p.getDescription().toLowerCase().contains(needle)))
                .toList();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        // Return a value from this method.
        return productRepository.findById(id)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    // Close the current code block.
    }
    @Transactional
    public Product createProduct(Product product, User actor, Long requestedShopId) {
        // Check a condition before running code.
        if (actor == null) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Unable to determine the authenticated user for product creation.");
        // Close the current code block.
        }

        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER")) {
            // Check a condition before running code.
            if (actor.getShop() == null) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("Assign this shop admin to a shop before creating products.");
            // Close the current code block.
            }
            product.setShop(actor.getShop());
        } else if (roleNames.contains("ROLE_ORGANIZATION_ADMIN")) {
            // Check a condition before running code.
            if (requestedShopId == null) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("Please select a shop for this product.");
            // Close the current code block.
            }

            var targetShop = shopRepository.findById(requestedShopId)
                    // Set a configuration key and value.
                    .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + requestedShopId));

            // Check a condition before running code.
            if (actor.getOrganization() == null || targetShop.getOrganization() == null ||
                    !actor.getOrganization().getId().equals(targetShop.getOrganization().getId())) {
                // Throw an exception for an error case.
                throw new IllegalArgumentException("You can only create products in shops from your organization.");
            // Close the current code block.
            }
            product.setShop(targetShop);
        } else {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Only organization admins and shop managers can create products.");
        // Close the current code block.
        }

        product.setSeller(actor);
        // Return a value from this method.
        return productRepository.save(product);
    // Close the current code block.
    }
    @Transactional
    public Product createProduct(Product product) {
        // Return a value from this method.
        return productRepository.save(product);
    // Close the current code block.
    }
    @Transactional
    public Product updateProduct(Long id, Product updated) {
        Product existing = getProductById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setLowStockThreshold(updated.getLowStockThreshold());
        existing.setCategory(updated.getCategory());
        // Return a value from this method.
        return productRepository.save(existing);
    // Close the current code block.
    }
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    // Close the current code block.
    }

    /**
     * Adjusts stock by a delta and records a StockTransaction.
     * For reductions (SALE, DECREASE), pass a negative quantity.
     * For additions (RESTOCK, INCREASE), pass a positive quantity.
     */
    @Transactional
    public Product adjustStock(Long productId, int quantityDelta, String type, String reason) {
        Product product = getProductById(productId);
        int newStock = product.getStockQuantity() + quantityDelta;

        // Check a condition before running code.
        if (newStock < 0) {
            // Throw an exception for an error case.
            throw new InsufficientStockException(
                "Insufficient stock for product '" + product.getName() +
                // Set a configuration key and value.
                "'. Available: " + product.getStockQuantity() +
                // Set a configuration key and value.
                ", Requested: " + Math.abs(quantityDelta)
            );
        // Close the current code block.
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);

        StockTransaction tx = StockTransaction.builder()
                .product(product)
                .quantity(quantityDelta)
                .type(type)
                .reason(reason)
                .build();
        stockTransactionRepository.save(tx);

        // Return a value from this method.
        return product;
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        // Return a value from this method.
        return productRepository.findLowStockProducts();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<StockTransaction> getStockHistory(Long productId) {
        Product product = getProductById(productId);
        // Return a value from this method.
        return stockTransactionRepository.findByProductOrderByCreatedAtDesc(product);
    // Close the current code block.
    }
// Close the current code block.
}
