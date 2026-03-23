// Define the package for this class.
package com.inventory.service;

import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Organization;
import com.inventory.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
// Define a public class.
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        // Return a value from this method.
        return organizationRepository.findAll();
    // Close the current code block.
    }
    @Transactional(readOnly = true)
    public Organization getById(Long id) {
        // Return a value from this method.
        return organizationRepository.findById(id)
                // Set a configuration key and value.
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
    // Close the current code block.
    }
    @Transactional
    public Organization create(String name) {
        Organization org = Organization.builder().name(name).build();
        // Return a value from this method.
        return organizationRepository.save(org);
    // Close the current code block.
    }
    @Transactional
    public Organization update(Long id, String name) {
        Organization org = getById(id);
        org.setName(name);
        // Return a value from this method.
        return organizationRepository.save(org);
    // Close the current code block.
    }
    @Transactional
    public void delete(Long id) {
        Organization org = getById(id);
        organizationRepository.delete(org);
    // Close the current code block.
    }
// Close the current code block.
}
