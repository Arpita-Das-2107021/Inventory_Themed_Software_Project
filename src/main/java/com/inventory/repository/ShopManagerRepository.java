package com.inventory.repository;

import com.inventory.model.Shop;
import com.inventory.model.ShopManager;
import com.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopManagerRepository extends JpaRepository<ShopManager, Long> {
    List<ShopManager> findByShop(Shop shop);
    List<ShopManager> findByUser(User user);
    Optional<ShopManager> findByUserAndShop(User user, Shop shop);
    boolean existsByUserAndShop(User user, Shop shop);
}
