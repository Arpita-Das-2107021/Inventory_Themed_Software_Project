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
public class ShopService {

    private final ShopRepository shopRepository;
    private final OrganizationRepository organizationRepository;
    private final ShopEmployeeRepository shopEmployeeRepository;
    private final ShopManagerRepository shopManagerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Shop> getAccessibleShops(User actor) {
        Set<String> roleNames = actor.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
        if (roleNames.contains("ROLE_ORGANIZATION_ADMIN") && actor.getOrganization() != null) {
            return shopRepository.findByOrganization(actor.getOrganization());
        }
        if (roleNames.contains("ROLE_SHOP_MANAGER") && actor.getShop() != null) {
            return List.of(actor.getShop());
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public List<Shop> getShopsByOrganization(Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        return shopRepository.findByOrganization(org);
    }

    @Transactional(readOnly = true)
    public Shop getById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop not found: " + id));
    }

    @Transactional
    public Shop create(String name, Long organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + organizationId));
        Shop shop = Shop.builder().name(name).organization(org).build();
        return shopRepository.save(shop);
    }

    @Transactional
    public Shop createForOwner(String name, User owner) {
        if (owner.getOrganization() == null) {
            throw new IllegalArgumentException("Owner account must belong to an organization.");
        }
        Shop shop = Shop.builder().name(name).organization(owner.getOrganization()).build();
        return shopRepository.save(shop);
    }

    @Transactional
    public Shop update(Long id, String name) {
        Shop shop = getById(id);
        shop.setName(name);
        return shopRepository.save(shop);
    }

    @Transactional
    public Shop updateForOwner(Long id, String name, User owner) {
        Shop shop = getById(id);
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
                !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            throw new IllegalArgumentException("You can only update shops in your own organization.");
        }
        shop.setName(name);
        return shopRepository.save(shop);
    }

    @Transactional
    public void delete(Long id) {
        Shop shop = getById(id);
        shopRepository.delete(shop);
    }

    @Transactional
    public void deleteForOwner(Long id, User owner) {
        Shop shop = getById(id);
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
                !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            throw new IllegalArgumentException("You can only delete shops in your own organization.");
        }
        shopRepository.delete(shop);
    }

    @Transactional
    public Shop assignManager(Long shopId, Long userId) {
        Shop shop = getById(shopId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        shop.setManager(user);
        user.setManager(true);
        userRepository.save(user);
        // Record in ShopManager table
        if (!shopManagerRepository.existsByUserAndShop(user, shop)) {
            ShopManager sm = ShopManager.builder().user(user).shop(shop).build();
            shopManagerRepository.save(sm);
        }
        return shopRepository.save(shop);
    }

    @Transactional
    public Shop assignManagerForOwner(Long shopId, Long userId, User owner) {
        Shop shop = getById(shopId);
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
            !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            throw new IllegalArgumentException("You can only assign managers in your own organization.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        user.setOrganization(shop.getOrganization());
        user.setShop(shop);
        user.setManager(true);
        userRepository.save(user);

        shop.setManager(user);
        if (!shopManagerRepository.existsByUserAndShop(user, shop)) {
            ShopManager sm = ShopManager.builder().user(user).shop(shop).build();
            shopManagerRepository.save(sm);
        }
        return shopRepository.save(shop);
    }

    @Transactional
    public ShopEmployee assignEmployee(Long shopId, Long userId) {
        Shop shop = getById(shopId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        if (shopEmployeeRepository.existsByUserAndShop(user, shop)) {
            return shopEmployeeRepository.findByUserAndShop(user, shop).orElseThrow();
        }
        ShopEmployee se = ShopEmployee.builder().user(user).shop(shop).build();
        return shopEmployeeRepository.save(se);
    }

    @Transactional
    public ShopEmployee assignEmployeeForManager(Long shopId, Long userId, User manager) {
        Shop shop = getById(shopId);
        if (manager.getShop() == null || !manager.getShop().getId().equals(shop.getId())) {
            throw new IllegalArgumentException("Shop admins can only manage employees in their assigned shop.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setShop(shop);
        user.setOrganization(shop.getOrganization());
        userRepository.save(user);

        if (shopEmployeeRepository.existsByUserAndShop(user, shop)) {
            return shopEmployeeRepository.findByUserAndShop(user, shop).orElseThrow();
        }
        ShopEmployee se = ShopEmployee.builder().user(user).shop(shop).build();
        return shopEmployeeRepository.save(se);
    }

    @Transactional
    public ShopEmployee assignEmployeeForOwner(Long shopId, Long userId, User owner) {
        Shop shop = getById(shopId);
        if (owner.getOrganization() == null || shop.getOrganization() == null ||
            !owner.getOrganization().getId().equals(shop.getOrganization().getId())) {
            throw new IllegalArgumentException("You can only assign employees in your own organization.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setShop(shop);
        user.setOrganization(shop.getOrganization());
        userRepository.save(user);

        if (shopEmployeeRepository.existsByUserAndShop(user, shop)) {
            return shopEmployeeRepository.findByUserAndShop(user, shop).orElseThrow();
        }
        ShopEmployee se = ShopEmployee.builder().user(user).shop(shop).build();
        return shopEmployeeRepository.save(se);
    }

    @Transactional(readOnly = true)
    public List<ShopEmployee> getEmployeesForShop(Long shopId) {
        Shop shop = getById(shopId);
        return shopEmployeeRepository.findByShop(shop);
    }
}
