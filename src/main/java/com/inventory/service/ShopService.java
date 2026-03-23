// Define the package for this class.
package com.inventory.service;

import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
@Service
@RequiredArgsConstructor
// Define a public class.
public class ShopService {
    private final ShopRepository shopRepository;
    private final OrganizationRepository organizationRepository;
    private final ShopEmployeeRepository shopEmployeeRepository;
    private final ShopManagerRepository shopManagerRepository;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public List<Shop> getAllShops() {
        // Return a value from this method.
        return shopRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Shop> getAccessibleShops(User actor) {
        // Set a configuration key and value.
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
        // Check a condition before running code.
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            // Return a value from this method.
            return shopRepository.findByOrganization(actor.getOrganization());
        // Close the current code block.
        }
        // Check a condition before running code.
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            // Return a value from this method.
            return List.of(actor.getShop());
        // Close the current code block.
        }
        // Return a value from this method.
        return List.of();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<Shop> getShopsByOrganization(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        // Return a value from this method.
        return shopRepository.findByOrganization(org);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Shop getById(Long id) {
        // Return a value from this method.
        return shopRepository.findById(id)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + id));
    // Close the current code block.
    }
    @Transactional
    public Shop create(String name, Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        Shop shop = Shop.builder().name(name).organization(org).build();
        // Return a value from this method.
        return shopRepository.save(shop);
    // Close the current code block.
    }
    @Transactional
    public Shop createForOwner(String name, User owner) {
        // Check a condition before running code.
        if (owner.getOrganization() == null) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Owner account must belong to an organization.");
        // Close the current code block.
        }
        Shop shop = Shop.builder().name(name).organization(owner.getOrganization()).build();
        // Return a value from this method.
        return shopRepository.save(shop);
    // Close the current code block.
    }
    @Transactional
    public Shop update(Long id, String name) {
        Shop shop = getById(id);
        shop.setName(name);
        // Return a value from this method.
        return shopRepository.save(shop);
    // Close the current code block.
    }
    @Transactional
    public Shop updateForOwner(Long id, String name, User owner) {
        Shop shop = getById(id);
        // Check a condition before running code.
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
                !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("You can only update shops in your own organization.");
        // Close the current code block.
        }
        shop.setName(name);
        // Return a value from this method.
        return shopRepository.save(shop);
    // Close the current code block.
    }
    @Transactional
    public void delete(Long id) {
        Shop shop = getById(id);
        shopRepository.delete(shop);
    // Close the current code block.
    }
    @Transactional
    public void deleteForOwner(Long id, User owner) {
        Shop shop = getById(id);
        // Check a condition before running code.
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
                !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("You can only delete shops in your own organization.");
        // Close the current code block.
        }
        shopRepository.delete(shop);
    // Close the current code block.
    }
    @Transactional
    public Shop assignManager(Long shopId, Long userId) {
        Shop shop = getById(shopId);
        User user = userRepository.findById(userId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        shop.setManager(user);
        user.setManager(true);
        userRepository.save(user);
        // Record in ShopManager table
        // Check a condition before running code.
        if (!shopManagerRepository.existsByUserAndShop(user, shop)) {
            ShopManager sm = ShopManager.builder().user(user).shop(shop).build();
            shopManagerRepository.save(sm);
        // Close the current code block.
        }
        // Return a value from this method.
        return shopRepository.save(shop);
    // Close the current code block.
    }
    @Transactional
    public Shop assignManagerForOwner(Long shopId, Long userId, User owner) {
        Shop shop = getById(shopId);
        // Check a condition before running code.
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
            !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("You can only assign managers in your own organization.");
        // Close the current code block.
        }

        User user = userRepository.findById(userId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setOrganization(shop.getOrganization());
        user.setShop(shop);
        user.setManager(true);
        userRepository.save(user);

        shop.setManager(user);
        // Check a condition before running code.
        if (!shopManagerRepository.existsByUserAndShop(user, shop)) {
            ShopManager sm = ShopManager.builder().user(user).shop(shop).build();
            shopManagerRepository.save(sm);
        // Close the current code block.
        }
        // Return a value from this method.
        return shopRepository.save(shop);
    // Close the current code block.
    }
    @Transactional
    public ShopEmployee assignEmployee(Long shopId, Long userId) {
        Shop shop = getById(shopId);
        User user = userRepository.findById(userId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        // Check a condition before running code.
        if (shopEmployeeRepository.existsByUserAndShop(user, shop)) {
            // Return a value from this method.
            return shopEmployeeRepository.findByUserAndShop(user, shop).orElseThrow();
        // Close the current code block.
        }
        ShopEmployee se = ShopEmployee.builder().user(user).shop(shop).build();
        // Return a value from this method.
        return shopEmployeeRepository.save(se);
    // Close the current code block.
    }
    @Transactional
    public ShopEmployee assignEmployeeForManager(Long shopId, Long userId, User manager) {
        Shop shop = getById(shopId);
        // Check a condition before running code.
        if (manager.getShop() == null || !manager.getShop().getId().equals(shop.getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("Shop admins can only manage employees in their assigned shop.");
        // Close the current code block.
        }

        User user = userRepository.findById(userId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setShop(shop);
        user.setOrganization(shop.getOrganization());
        userRepository.save(user);

        // Check a condition before running code.
        if (shopEmployeeRepository.existsByUserAndShop(user, shop)) {
            // Return a value from this method.
            return shopEmployeeRepository.findByUserAndShop(user, shop).orElseThrow();
        // Close the current code block.
        }
        ShopEmployee se = ShopEmployee.builder().user(user).shop(shop).build();
        // Return a value from this method.
        return shopEmployeeRepository.save(se);
    // Close the current code block.
    }
    @Transactional
    public ShopEmployee assignEmployeeForOwner(Long shopId, Long userId, User owner) {
        Shop shop = getById(shopId);
        // Check a condition before running code.
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
            !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            // Throw an exception for an error case.
            throw new IllegalArgumentException("You can only assign employees in your own organization.");
        // Close the current code block.
        }

        User user = userRepository.findById(userId)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setShop(shop);
        user.setOrganization(shop.getOrganization());
        userRepository.save(user);

        // Check a condition before running code.
        if (shopEmployeeRepository.existsByUserAndShop(user, shop)) {
            // Return a value from this method.
            return shopEmployeeRepository.findByUserAndShop(user, shop).orElseThrow();
        // Close the current code block.
        }
        ShopEmployee se = ShopEmployee.builder().user(user).shop(shop).build();
        // Return a value from this method.
        return shopEmployeeRepository.save(se);
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public List<ShopEmployee> getEmployeesForShop(Long shopId) {
        Shop shop = getById(shopId);
        // Return a value from this method.
        return shopEmployeeRepository.findByShop(shop);
    // Close the current code block.
    }
// Close the current code block.
}
