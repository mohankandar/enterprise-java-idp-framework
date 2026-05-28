package io.github.mohankandar.idp.demo.service;

import io.github.mohankandar.idp.core.error.IdpException;
import io.github.mohankandar.idp.demo.domain.Customer;
import io.github.mohankandar.idp.demo.dto.CustomerDto;
import io.github.mohankandar.idp.demo.repo.CustomerRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repo;

    private SimpleMeterRegistry meterRegistry;
    private CustomerService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new CustomerService(repo, meterRegistry);
    }

    @Test
    void createSavesCustomerAndIncrementsMetric() {
        CustomerDto dto = new CustomerDto("Jane", "Doe", "jane@example.com");
        Customer saved = new Customer(dto.firstName(), dto.lastName(), dto.email());

        when(repo.save(any(Customer.class))).thenReturn(saved);

        Customer result = service.create(dto);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("Jane");
        assertThat(captor.getValue().getLastName()).isEqualTo("Doe");
        assertThat(captor.getValue().getEmail()).isEqualTo("jane@example.com");
        assertThat(result).isSameAs(saved);
        assertThat(meterRegistry.get("demo.customers.created").counter().count()).isEqualTo(1.0d);
    }

    @Test
    void findByIdDelegatesToRepository() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer("Ava", "Stone", "ava@example.com");
        when(repo.findById(id)).thenReturn(Optional.of(customer));

        Optional<Customer> result = service.findById(id);

        assertThat(result).contains(customer);
    }

    @Test
    void searchBuildsExpectedPageRequest() {
        when(repo.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        service.search(" Jane ", "Example", Instant.parse("2026-01-01T00:00:00Z"), 2, 5, "firstName,asc;email,desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("firstName").getDirection().name()).isEqualTo("ASC");
        assertThat(pageable.getSort().getOrderFor("email").getDirection().name()).isEqualTo("DESC");
    }

    @Test
    void searchFallsBackToDefaultSortAndBounds() {
        when(repo.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        service.search(null, null, null, -3, 0, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(1);
        assertThat(pageable.getSort().getOrderFor("createdDate").getDirection().name()).isEqualTo("DESC");
    }

    @Test
    void updateThrowsWhenCustomerDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new CustomerDto("J", "D", "jd@example.com")))
            .isInstanceOf(IdpException.class)
            .hasMessageContaining("Customer not found");

        verify(repo, never()).save(any(Customer.class));
    }

    @Test
    void deleteThrowsWhenCustomerDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repo.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(IdpException.class)
            .hasMessageContaining("Customer not found");

        verify(repo, never()).deleteById(any(UUID.class));
    }
}
