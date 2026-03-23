// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

// Define an interface.
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
    List<AuditLog> findTop50ByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
    List<AuditLog> findTop50ByShopIdOrderByCreatedAtDesc(Long shopId);
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<AuditLog> findByUserEmailOrderByCreatedAtDesc(String userEmail);
// Close the current code block.
}
