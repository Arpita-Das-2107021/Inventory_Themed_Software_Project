// Define the package for this class.
package com.inventory.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
@ControllerAdvice
// Define a public class.
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        // Return a value from this method.
        return "error/404";
    // Close the current code block.
    }
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInsufficientStock(InsufficientStockException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        // Return a value from this method.
        return "error/400";
    // Close the current code block.
    }
    @ExceptionHandler(DuplicateEmailException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleDuplicateEmail(DuplicateEmailException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        // Return a value from this method.
        return "error/400";
    // Close the current code block.
    }
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("message", ex.getMessage());
        return "error/403";
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(Exception ex, Model model) {
        model.addAttribute("message", "An unexpected error occurred. Please try again.");
        // Return a value from this method.
        return "error/500";
    // Close the current code block.
    }
// Close the current code block.
}
