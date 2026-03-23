// Define the package for this class.
package com.inventory.repository;

import com.inventory.model.Shop;
import com.inventory.model.ShopEmployee;
import com.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
// Define an interface.
public interface ShopEmployeeRepository extends JpaRepository<ShopEmployee, Long> {
    List<ShopEmployee> findByShop(Shop shop);
    List<ShopEmployee> findByUser(User user);
    Optional<ShopEmployee> findByUserAndShop(User user, Shop shop);
    boolean existsByUserAndShop(User user, Shop shop);
// Close the current code block.
}
