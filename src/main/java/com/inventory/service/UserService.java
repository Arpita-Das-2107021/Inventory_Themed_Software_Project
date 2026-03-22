// Define the package for this class.
package com.inventory.service;

import com.inventory.dto.RegisterRequest;
import com.inventory.exception.DuplicateEmailException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Organization;
import com.inventory.model.Role;
import com.inventory.model.User;
import com.inventory.repository.OrganizationRepository;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
// Define a public class.
public class UserService {
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public User register(RegisterRequest request) {
        // Check a condition before running code.
        if (userRepository.existsByEmail(request.getEmail())) {
            // Throw an exception for an error case.
            throw new DuplicateEmailException("Email already in use: " + request.getEmail());
        // Close the current code block.
        }

        final String resolvedRoleName = "ROLE_ORGANIZATION_ADMIN";
        Role role = roleRepository.findByName(resolvedRoleName)
            // Set a configuration key and value.
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + resolvedRoleName));

        Organization organization = Organization.builder()
            .name(request.getUsername() + " Company")
            .build();
        organization = organizationRepository.save(organization);

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
            .organization(organization)
                .active(true)
                .build();
        user.getRoles().add(role);

        // Return a value from this method.
        return userRepository.save(user);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        // Return a value from this method.
        return userRepository.findByEmail(email);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        // Return a value from this method.
        return userRepository.findByEmail(email)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    // Close the current code block.
    }
    @Transactional
    public User ensureOwnerOrganization(User owner) {
        boolean isOwner = owner.getRoles().stream().anyMatch(r -> "ROLE_ORGANIZATION_ADMIN".equals(r.getName()));
        // Check a condition before running code.
        if (!isOwner) {
            // Return a value from this method.
            return owner;
        // Close the current code block.
        }
        // Check a condition before running code.
        if (owner.getOrganization() != null) {
            // Return a value from this method.
            return owner;
        // Close the current code block.
        }

        Organization organization = Organization.builder()
                .name(owner.getUsername() + " Company")
                .build();
        organization = organizationRepository.save(organization);
        owner.setOrganization(organization);
        // Return a value from this method.
        return userRepository.save(owner);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        // Return a value from this method.
        return userRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<User> getUsersByOrganization(Long organizationId) {
        // Return a value from this method.
        return userRepository.findByOrganizationId(organizationId);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<User> getUsersByShop(Long shopId) {
        // Return a value from this method.
        return userRepository.findByShopId(shopId);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public User getById(Long id) {
        // Return a value from this method.
        return userRepository.findById(id)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    // Close the current code block.
    }

    /** Toggle active/inactive status */
    @Transactional
    public User toggleActive(Long id) {
        User user = getById(id);
        user.setActive(!user.isActive());
        // Return a value from this method.
        return userRepository.save(user);
    // Close the current code block.
    }

    /** Admin changes a user's role */
    @Transactional
    public User changeRole(Long id, String roleName) {
        User user = getById(id);
        Role role = roleRepository.findByName(roleName)
                // Set a configuration key and value.
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        user.getRoles().clear();
        user.getRoles().add(role);
        // Return a value from this method.
        return userRepository.save(user);
    // Close the current code block.
    }

    /** Admin resets a user's password */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    // Close the current code block.
    }
    @Transactional
    public User createSeller(String username, String email, String password) {
        // Return a value from this method.
        return createUser(username, email, password, "ROLE_EMPLOYEE", null, null);
    // Close the current code block.
    }
    @Transactional
    public User createUser(String username, String email, String password, String roleName) {
        // Return a value from this method.
        return createUser(username, email, password, roleName, null, null);
    // Close the current code block.
    }
    @Transactional
    public User createUser(String username, String email, String password, String roleName,
                           Organization organization, com.inventory.model.Shop shop) {
        // Check a condition before running code.
        if (userRepository.existsByEmail(email)) {
            // Throw an exception for an error case.
            throw new DuplicateEmailException("Email already in use: " + email);
        // Close the current code block.
        }
        // Check a condition before running code.
        if (shop != null && organization != null && shop.getOrganization() != null &&
                !shop.getOrganization().getId().equals(organization.getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Selected shop does not belong to the provided organization.");
        // Close the current code block.
        }
        // Check a condition before running code.
        if (organization == null && shop != null) {
            organization = shop.getOrganization();
        // Close the current code block.
        }
        Role role = roleRepository.findByName(roleName)
                // Set a configuration key and value.
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .organization(organization)
                .shop(shop)
                .manager("ROLE_SHOP_MANAGER".equals(roleName))
                .active(true)
                .build();
        user.getRoles().add(role);
        // Return a value from this method.
        return userRepository.save(user);
    // Close the current code block.
    }
    @Transactional
    public User updateUser(Long id, String username, String roleName) {
        User user = getById(id);
        user.setUsername(username);
        Role role = roleRepository.findByName(roleName)
                // Set a configuration key and value.
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        user.getRoles().clear();
        user.getRoles().add(role);
        // Return a value from this method.
        return userRepository.save(user);
    // Close the current code block.
    }
// Close the current code block.
}

