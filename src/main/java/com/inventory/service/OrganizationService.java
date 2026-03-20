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
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Organization getById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found: " + id));
    }

    @Transactional
    public Organization create(String name) {
        Organization org = Organization.builder().name(name).build();
        return organizationRepository.save(org);
    }

    @Transactional
    public Organization update(Long id, String name) {
        Organization org = getById(id);
        org.setName(name);
        return organizationRepository.save(org);
    }

    @Transactional
    public void delete(Long id) {
        Organization org = getById(id);
        organizationRepository.delete(org);
    }
}
