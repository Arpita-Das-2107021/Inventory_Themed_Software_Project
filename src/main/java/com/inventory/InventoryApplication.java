// Define the package for this class.
package com.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
@SpringBootApplication
@EnableAsync
// Define a public class.
public class InventoryApplication {
    // Start the main entry method.
    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    // Close the current code block.
    }
// Close the current code block.
}
