// Define the package for this class.
package com.inventory.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "shops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Define a public class.
public class Shop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();
// Close the current code block.
}
