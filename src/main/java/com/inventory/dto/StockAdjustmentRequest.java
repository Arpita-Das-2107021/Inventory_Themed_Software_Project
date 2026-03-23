// Define the package for this class.
package com.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
// Define a public class.
public class StockAdjustmentRequest {
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    @NotBlank(message = "Transaction type is required")
    private String type;  // INCREASE, DECREASE, RESTOCK
    private String reason;
// Close the current code block.
}
