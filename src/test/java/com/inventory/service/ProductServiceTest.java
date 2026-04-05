// Define the package for this class.
package com.inventory.service;

import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Product;
import com.inventory.model.StockTransaction;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
// Define a class.
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockTransactionRepository stockTransactionRepository;
    @InjectMocks
    private ProductService productService;
    private Product sampleProduct;
    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("25.00"))
                .stockQuantity(50)
                .lowStockThreshold(10)
                .build();
    // Close the current code block.
    }

    // Test 1
    @Test
    void createProduct_shouldSaveAndReturn() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        Product result = productService.createProduct(sampleProduct);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Mouse");
        verify(productRepository, times(1)).save(sampleProduct);
    // Close the current code block.
    }

    // Test 2
    @Test
    void getProductById_shouldThrowWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    // Close the current code block.
    }

    // Test 3
    @Test
    void adjustStock_shouldReduceQuantityAndCreateTransaction() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
        when(stockTransactionRepository.save(any(StockTransaction.class))).thenReturn(new StockTransaction());

        productService.adjustStock(1L, -5, "SALE", "Sold in sale #1");

        assertThat(sampleProduct.getStockQuantity()).isEqualTo(45);
        verify(stockTransactionRepository, times(1)).save(any(StockTransaction.class));
    // Close the current code block.
    }

    // Test 4
    @Test
    void adjustStock_shouldThrowWhenStockGoesNegative() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        assertThatThrownBy(() -> productService.adjustStock(1L, -100, "SALE", "oversell"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    // Close the current code block.
    }

    // Test 5
    @Test
    void getLowStockProducts_shouldReturnOnlyLowStockItems() {
        Product lowProduct = Product.builder()
                .id(2L).name("Keyboard").price(BigDecimal.TEN)
                .stockQuantity(3).lowStockThreshold(10).build();

        when(productRepository.findLowStockProducts()).thenReturn(List.of(lowProduct));

        List<Product> result = productService.getLowStockProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Keyboard");
        assertThat(result.get(0).isLowStock()).isTrue();
    // Close the current code block.
    }

    // Test 6
    @Test
    void deleteProduct_shouldMarkInactiveAndSave() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        productService.deleteProduct(1L);

        assertThat(sampleProduct.isActive()).isFalse();
        verify(productRepository, times(1)).save(sampleProduct);
    // Close the current code block.
    }
// Close the current code block.
}
