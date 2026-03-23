// Define the package for this class.
package com.inventory.controller;

import com.inventory.service.ReportService;
import com.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
@Controller
@RequestMapping("/reports")
@PreAuthorize("hasAnyRole('SHOP_MANAGER', 'ORGANIZATION_ADMIN')")
@RequiredArgsConstructor
// Define a public class.
public class ReportController {
    private final ReportService reportService;
    private final UserService userService;
    @GetMapping("/sales")
    public String salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        // Check a condition before running code.
        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        // Check a condition before running code.
        if (to == null) to = LocalDate.now();

        var actor = userService.getByEmail(userDetails.getUsername());
        model.addAttribute("sales", reportService.getSalesReportForUser(from, to, actor));
        model.addAttribute("totalRevenue", reportService.getTotalRevenueBetweenForUser(from, to, actor));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        // Return a value from this method.
        return "reports/sales-report";
    // Close the current code block.
    }
    @GetMapping("/inventory")
    public String inventoryReport(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var actor = userService.getByEmail(userDetails.getUsername());
        model.addAttribute("products", reportService.getInventoryReportForUser(actor));
        // Return a value from this method.
        return "reports/inventory-report";
    // Close the current code block.
    }
    @GetMapping("/seller-activity")
    public String sellerActivityReport(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        var actor = userService.getByEmail(userDetails.getUsername());
        model.addAttribute("sellerStats", reportService.getSellerActivityReportForUser(actor));
        // Return a value from this method.
        return "reports/seller-activity";
    // Close the current code block.
    }
// Close the current code block.
}
