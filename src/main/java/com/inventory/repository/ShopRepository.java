package com.inventory.repository;

import com.inventory.model.Organization;
import com.inventory.model.Shop;
import com.inventory.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByOrganization(Organization organization);
    List<Shop> findByManager(User manager);
    Optional<Shop> findByNameAndOrganization(String name, Organization organization);
}
