// Define the package for this class.
package com.inventory.controller;

import com.inventory.dto.RegisterRequest;
import com.inventory.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
// Define a public class.
public class AuthController {
    private final UserService userService;
    @GetMapping("/login")
    public String loginPage() {
        // Return a value from this method.
        return "auth/login";
    // Close the current code block.
    }
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        // Return a value from this method.
        return "auth/register";
    // Close the current code block.
    }
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Check a condition before running code.
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.registerRequest", "Passwords do not match");
        // Close the current code block.
        }

        // Check a condition before running code.
        if (bindingResult.hasErrors()) {
            // Return a value from this method.
            return "auth/register";
        // Close the current code block.
        }

        try {
            userService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            // Return a value from this method.
            return "redirect:/auth/login";
        } catch (com.inventory.exception.DuplicateEmailException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // Return a value from this method.
            return "auth/register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            // Return a value from this method.
            return "auth/register";
        // Close the current code block.
        }
    // Close the current code block.
    }
// Close the current code block.
}
