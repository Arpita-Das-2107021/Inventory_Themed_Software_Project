// Define the package for this class.
package com.inventory.service;

import com.inventory.dto.SaleItemRequest;
import com.inventory.dto.SaleRequest;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.*;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
// Define a class.
class SaleServiceTest {
    @Mock
    private SaleRepository saleRepository;
    @Mock
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private SaleService saleService;
    private User seller;
    private Product product;
    @BeforeEach
    void setUp() {
        Organization org = Organization.builder().id(100L).name("Acme").build();
        Shop shop = Shop.builder().id(200L).name("Main Shop").organization(org).build();
        Role shopManagerRole = Role.builder().id(1L).name("ROLE_SHOP_MANAGER").build();

        seller = User.builder()
            .id(1L)
            .username("john")
            .email("john@test.com")
            .password("hashed")
            .organization(org)
            .shop(shop)
            .roles(java.util.Set.of(shopManagerRole))
            .build();

        product = Product.builder()
                .id(10L).name("Mouse").price(new BigDecimal("20.00"))
            .stockQuantity(30).lowStockThreshold(5)
            .shop(shop)
            .active(true)
            .build();
    // Close the current code block.
    }
    private SaleRequest buildRequest(int qty) {
        SaleItemRequest itemReq = new SaleItemRequest();
        itemReq.setProductId(10L);
        itemReq.setQuantity(qty);

        SaleRequest req = new SaleRequest();
        req.setItems(List.of(itemReq));
        req.setBuyerName("Test Buyer");
        // Return a value from this method.
        return req;
    // Close the current code block.
    }

    // Test 7
    @Test
    void createSale_shouldCreateSaleAndSaleItems() {
        when(productService.getProductById(10L)).thenReturn(product);
        when(productService.adjustStock(anyLong(), anyInt(), anyString(), anyString()))
                .thenReturn(product);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
            Sale s = inv.getArgument(0);
            s = Sale.builder()
                    .id(1L).seller(seller)
                    .totalAmount(s.getTotalAmount())
                    .saleItems(new ArrayList<>(s.getSaleItems())).build();
            // Return a value from this method.
            return s;
        });

        Sale result = saleService.createSale(buildRequest(2), seller);

        assertThat(result.getSaleItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("40.00");
    // Close the current code block.
    }

    // Test 8
    @Test
    void createSale_shouldReduceProductStock() {
        when(productService.getProductById(10L)).thenReturn(product);
        when(productService.adjustStock(anyLong(), anyInt(), anyString(), anyString()))
                .thenReturn(product);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        saleService.createSale(buildRequest(3), seller);

        verify(productService).adjustStock(eq(10L), eq(-3), eq("SALE"), anyString());
    // Close the current code block.
    }

    // Test 9
    @Test
    void createSale_shouldCreateStockTransaction() {
        when(productService.getProductById(10L)).thenReturn(product);
        when(productService.adjustStock(anyLong(), anyInt(), any(), any()))
                .thenReturn(product);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        saleService.createSale(buildRequest(1), seller);

        // adjustStock is the point where StockTransaction is created
        verify(productService, times(1)).adjustStock(eq(10L), eq(-1), eq("SALE"), anyString());
    // Close the current code block.
    }

    // Test 10
    @Test
    void createSale_shouldThrowWhenStockInsufficient() {
        product = Product.builder()
                .id(10L).name("Mouse").price(new BigDecimal("20.00"))
            .stockQuantity(1).lowStockThreshold(5)
            .shop(seller.getShop())
            .active(true)
            .build();
        when(productService.getProductById(10L)).thenReturn(product);

        assertThatThrownBy(() -> saleService.createSale(buildRequest(5), seller))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    // Close the current code block.
    }

    // Test 11
    @Test
    void getSaleById_shouldThrowWhenNotFound() {
        when(saleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getSaleById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    // Close the current code block.
    }
// Close the current code block.
}
