package com.inventory.service;

import com.inventory.model.Organization;
import com.inventory.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    OrganizationRepository organizationRepository;

    @InjectMocks
    OrganizationService organizationService;

    private Organization org;

    @BeforeEach
    void setUp() {
        org = Organization.builder().id(1L).name("Acme").build();
    }

    @Test
    void create_savesOrganization() {
        when(organizationRepository.save(any(Organization.class))).thenReturn(org);

        Organization result = organizationService.create("Acme");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Acme");
    }

    @Test
    void getById_missing_throws() {
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> organizationService.getById(99L))
                .isInstanceOf(RuntimeException.class);
    }
}
