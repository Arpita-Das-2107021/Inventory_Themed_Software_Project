package com.inventory.config;

// import all models (Entity classes like User, Product, etc.)
import com.inventory.model.*;

// import all repositories (used to access database)
import com.inventory.repository.*;

// Lombok: creates constructor automatically
import lombok.RequiredArgsConstructor;

// Lombok: for logging (log.info)
import lombok.extern.slf4j.Slf4j;

// runs code after app starts
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

// used to run raw SQL queries
import org.springframework.jdbc.core.JdbcTemplate;

// used to encrypt passwords
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Profile;

// marks this as a Spring component (auto-run)
import org.springframework.stereotype.Component;

// ensures all DB operations run safely
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Slf4j // enables logging
@Component // Spring will detect and run this class
@Profile("!test")
@RequiredArgsConstructor // constructor auto-generated
public class DataSeeder implements ApplicationRunner {

    // dependencies injected by Spring
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    // repositories for DB operations
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final AuditLogRepository auditLogRepository;

    // runs automatically when app starts
    @Override
    @Transactional // all DB operations in one transaction
    public void run(ApplicationArguments args) {
        log.info("=== Clearing old data and seeding new data ===");

        clearAllData(); // delete old data
        seedAll();      // insert new data

        log.info("=== Seeding complete ===");
    }

    // delete all existing data from database
    private void clearAllData() {
        jdbcTemplate.execute(
            "TRUNCATE TABLE audit_logs, stock_transactions, sale_items, sales, " +
            "shop_employees, shop_managers, user_roles, products, users, shops, " +
            "organizations, categories, roles " +
            "RESTART IDENTITY CASCADE"
        );
        log.info("All tables cleared.");
    }

    // main method to insert demo data
    private void seedAll() {

        // 1. Create roles (admin, manager, etc.)
        Role roleAdmin    = roleRepository.save(Role.builder().name("ROLE_ORGANIZATION_ADMIN").build());
        Role roleManager  = roleRepository.save(Role.builder().name("ROLE_SHOP_MANAGER").build());
        Role roleEmployee = roleRepository.save(Role.builder().name("ROLE_EMPLOYEE").build());

        // 2. Create organization (company)
        Organization org = organizationRepository.save(
            Organization.builder().name("TechMart Global").build()
        );

        // 3. Create users and assign roles
        userRepository.save(User.builder()
            .username("Admin User")
            .email("admin@techmart.com")
            .password(passwordEncoder.encode("Admin@123")) // encrypted password
            .active(true)
            .manager(false)
            .organization(org)
            .roles(new HashSet<>(List.of(roleAdmin)))
            .build());

        // more users (manager, employees)
        User mgr1 = userRepository.save(User.builder()
            .username("Downtown Manager")
            .email("manager.downtown@techmart.com")
            .password(passwordEncoder.encode("Manager@123"))
            .manager(true)
            .organization(org)
            .roles(new HashSet<>(List.of(roleManager)))
            .build());

        User mgr2 = userRepository.save(User.builder()
            .username("Mall Manager")
            .email("manager.mall@techmart.com")
            .password(passwordEncoder.encode("Manager@123"))
            .manager(true)
            .organization(org)
            .roles(new HashSet<>(List.of(roleManager)))
            .build());

        User emp1 = userRepository.save(User.builder()
            .username("Alice Smith")
            .email("alice@techmart.com")
            .password(passwordEncoder.encode("Employee@123"))
            .organization(org)
            .roles(new HashSet<>(List.of(roleEmployee)))
            .build());

        // (other users similar...)

        // 4. Create shops
        Shop shop1 = shopRepository.save(Shop.builder().name("Downtown Store").organization(org).manager(mgr1).build());
        Shop shop2 = shopRepository.save(Shop.builder().name("Mall Branch").organization(org).manager(mgr2).build());
        Shop shop3 = shopRepository.save(Shop.builder().name("Suburb Outlet").organization(org).manager(mgr1).build());

        // assign users to shops
        mgr1.setShop(shop1);
        mgr2.setShop(shop2);
        emp1.setShop(shop1);
        userRepository.saveAll(List.of(mgr1, mgr2, emp1));

        // 5. Categories (product types)
        Category catElec = categoryRepository.save(Category.builder().name("Electronics").build());
        Category catOffice = categoryRepository.save(Category.builder().name("Office Supplies").build());
        Category catGrocery = categoryRepository.save(Category.builder().name("Groceries").build());
        Category catTools = categoryRepository.save(Category.builder().name("Tools & Hardware").build());

        // 6. Products
        Product p1 = productRepository.save(Product.builder()
            .name("Laptop Pro 15")
            .description("15-inch business laptop")
            .price(bd("1299.99")) // helper function
            .stockQuantity(50)
            .lowStockThreshold(5)
            .category(catElec)
            .seller(emp1)
            .shop(shop1)
            .build());

        Product p2 = productRepository.save(Product.builder()
            .name("Wireless Mouse")
            .description("Ergonomic Bluetooth mouse")
            .price(bd("29.99"))
            .stockQuantity(120)
            .lowStockThreshold(15)
            .category(catElec)
            .seller(emp1)
            .shop(shop1)
            .build());

        Product p3 = productRepository.save(Product.builder()
            .name("Mechanical Keyboard")
            .description("Backlit keyboard with blue switches")
            .price(bd("89.99"))
            .stockQuantity(75)
            .lowStockThreshold(10)
            .category(catElec)
            .seller(mgr1)
            .shop(shop1)
            .build());

        Product p4 = productRepository.save(Product.builder()
            .name("Office Chair")
            .description("Adjustable mesh office chair")
            .price(bd("199.99"))
            .stockQuantity(35)
            .lowStockThreshold(5)
            .category(catOffice)
            .seller(mgr2)
            .shop(shop2)
            .build());

        Product p5 = productRepository.save(Product.builder()
            .name("A4 Paper Pack")
            .description("500-sheet multipurpose paper")
            .price(bd("6.49"))
            .stockQuantity(300)
            .lowStockThreshold(40)
            .category(catOffice)
            .seller(mgr2)
            .shop(shop2)
            .build());

        Product p6 = productRepository.save(Product.builder()
            .name("LED Desk Lamp")
            .description("Dimmable USB desk lamp")
            .price(bd("24.99"))
            .stockQuantity(90)
            .lowStockThreshold(12)
            .category(catElec)
            .seller(mgr2)
            .shop(shop2)
            .build());

        Product p7 = productRepository.save(Product.builder()
            .name("Hand Tool Kit")
            .description("32-piece household tool set")
            .price(bd("49.99"))
            .stockQuantity(60)
            .lowStockThreshold(8)
            .category(catTools)
            .seller(mgr1)
            .shop(shop3)
            .build());

        Product p8 = productRepository.save(Product.builder()
            .name("Rice 5kg")
            .description("Premium long-grain rice")
            .price(bd("12.99"))
            .stockQuantity(140)
            .lowStockThreshold(20)
            .category(catGrocery)
            .seller(mgr1)
            .shop(shop3)
            .build());

        // 7. Sales (transactions)
        Sale sale1 = saleRepository.save(Sale.builder()
            .seller(emp1)
            .buyerName("Walk-in Buyer")
            .shop(shop1)
            .totalAmount(bd("1329.98"))
            .build());

        // sale items (what products were sold)
        saleItemRepository.save(SaleItem.builder()
            .sale(sale1)
            .product(p1)
            .quantity(1)
            .price(p1.getPrice())
            .subtotal(p1.getPrice())
            .build());

        // 8. Stock transactions (inventory changes)
        stockTransactionRepository.save(
            tx(p1, 50, "RESTOCK", "Initial stock", shop1)
        );
        stockTransactionRepository.save(tx(p2, 120, "RESTOCK", "Initial stock", shop1));
        stockTransactionRepository.save(tx(p3, 75, "RESTOCK", "Initial stock", shop1));
        stockTransactionRepository.save(tx(p4, 35, "RESTOCK", "Initial stock", shop2));
        stockTransactionRepository.save(tx(p5, 300, "RESTOCK", "Initial stock", shop2));
        stockTransactionRepository.save(tx(p6, 90, "RESTOCK", "Initial stock", shop2));
        stockTransactionRepository.save(tx(p7, 60, "RESTOCK", "Initial stock", shop3));
        stockTransactionRepository.save(tx(p8, 140, "RESTOCK", "Initial stock", shop3));

        // 9. Audit logs (activity tracking)
        auditLogRepository.save(AuditLog.builder()
            .userEmail("admin@techmart.com")
            .action("CREATE")
            .entityType("Organization")
            .entityId(str(org.getId()))
            .details("Organization created")
            .build());

        log.info("Seeding finished successfully.");
    }

    // helper: convert string to BigDecimal
    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }

    // helper: convert ID to string safely
    private String str(Long id) {
        return id == null ? "" : id.toString();
    }

    // helper: create stock transaction object
    private StockTransaction tx(Product product, int qty, String type, String reason, Shop shop) {
        return StockTransaction.builder()
            .product(product)
            .quantity(qty)
            .type(type)
            .reason(reason)
            .shop(shop)
            .build();
    }
}