// Define the package for this class.
package com.inventory.service;

import com.inventory.model.AuditLog;
import com.inventory.model.User;
import com.inventory.repository.AuditLogRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
// Define a public class.
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String userEmail, String action, String entityType, String entityId, String details) {
        User actor = userRepository.findByEmail(userEmail).orElse(null);
        AuditLog entry = AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
            // Set a configuration key and value.
            .organizationId(actor != null && actor.getOrganization() != null ? actor.getOrganization().getId() : null)
            // Set a configuration key and value.
            .shopId(actor != null && actor.getShop() != null ? actor.getShop().getId() : null)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogs() {
        // Return a value from this method.
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogsForOrganization(Long organizationId) {
        // Return a value from this method.
        return auditLogRepository.findTop50ByOrganizationIdOrderByCreatedAtDesc(organizationId);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentLogsForShop(Long shopId) {
        // Return a value from this method.
        return auditLogRepository.findTop50ByShopIdOrderByCreatedAtDesc(shopId);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<AuditLog> getLogsByUser(String userEmail) {
        // Return a value from this method.
        return auditLogRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
    // Close the current code block.
    }
// Close the current code block.
}
