// Define the package for this class.
package com.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
// Define a public class.
public class SaleItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
// Close the current code block.
}
