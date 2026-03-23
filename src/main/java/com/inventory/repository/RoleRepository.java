// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Define an interface.
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
// Close the current code block.
}
