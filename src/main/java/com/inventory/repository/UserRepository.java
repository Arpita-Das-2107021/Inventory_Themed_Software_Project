// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Define an interface.
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByOrganizationId(Long organizationId);
    List<User> findByShopId(Long shopId);
    long countByOrganizationId(Long organizationId);
// Close the current code block.
}
