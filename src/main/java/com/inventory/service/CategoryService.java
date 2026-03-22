// Define the package for this class.
package com.inventory.service;

import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Category;
import com.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
// Define a public class.
public class CategoryService {
    private final CategoryRepository categoryRepository;
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        // Return a value from this method.
        return categoryRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Category getById(Long id) {
        // Return a value from this method.
        return categoryRepository.findById(id)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    // Close the current code block.
    }
    @Transactional
    public Category create(Category category) {
        // Return a value from this method.
        return categoryRepository.save(category);
    // Close the current code block.
    }
    @Transactional
    public Category update(Long id, Category updated) {
        Category existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        // Return a value from this method.
        return categoryRepository.save(existing);
    // Close the current code block.
    }
    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getById(id));
    // Close the current code block.
    }
// Close the current code block.
}
