// Define the package where this service belongs
package com.inventory.service;

// Custom exception when resource is not found
import com.inventory.exception.ResourceNotFoundException;

// Category model (data object)
import com.inventory.model.Category;

// Repository for database operations
import com.inventory.repository.CategoryRepository;

// Lombok: auto constructor for final fields
import lombok.RequiredArgsConstructor;

// Marks this class as a service (business logic layer)
import org.springframework.stereotype.Service;

// Used for database transaction management
import org.springframework.transaction.annotation.Transactional;

// List collection
import java.util.List;

// Marks this class as Spring service
@Service

// Generates constructor automatically
@RequiredArgsConstructor

// Service class for Category
public class CategoryService {

    // Repository to interact with database
    private final CategoryRepository categoryRepository;

    // ===================== GET ALL CATEGORIES =====================
    @Transactional(readOnly = true) // read-only for performance
    public List<Category> getAllCategories() {

        // Fetch all categories from DB
        return categoryRepository.findAll();
    }

    // ===================== GET CATEGORY BY ID =====================
    @Transactional(readOnly = true)
    public Category getById(Long id) {

        // Try to find category, if not found → throw exception
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found: " + id)
                );
    }

    // ===================== CREATE CATEGORY =====================
    @Transactional
    public Category create(Category category) {

        // Save new category into DB
        return categoryRepository.save(category);
    }

    // ===================== UPDATE CATEGORY =====================
    @Transactional
    public Category update(Long id, Category updated) {

        // Get existing category (or throw error if not found)
        Category existing = getById(id);

        // Update fields
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());

        // Save updated category
        return categoryRepository.save(existing);
    }

    // ===================== DELETE CATEGORY =====================
    @Transactional
    public void delete(Long id) {

        // First find category, then delete it
        categoryRepository.delete(getById(id));
    }
}