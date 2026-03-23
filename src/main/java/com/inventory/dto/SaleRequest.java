// Define the package for this class.
package com.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
@Data
// Define a public class.
public class SaleRequest {
    @NotBlank(message = "Buyer name is required")
    private String buyerName;

    @NotEmpty(message = "A sale must have at least one item")
    @Valid
    private List<SaleItemRequest> items;
// Close the current code block.
}
