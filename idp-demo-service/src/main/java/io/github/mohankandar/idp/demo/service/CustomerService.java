package io.github.mohankandar.idp.demo.service;

import io.github.mohankandar.idp.demo.domain.Customer;
import io.github.mohankandar.idp.demo.dto.CustomerDto;
import io.github.mohankandar.idp.demo.repo.CustomerRepository;
import io.github.mohankandar.idp.core.api.ErrorCode;
import io.github.mohankandar.idp.core.api.ErrorDetail;
import io.github.mohankandar.idp.core.error.IdpException;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository repo;
    private final MeterRegistry registry;

    public CustomerService(CustomerRepository repo, MeterRegistry registry) {
        this.repo = repo;
        this.registry = registry;
    }

    public Customer create(CustomerDto dto) {
        log.info("Creating customer [email masked]");
        var c = new Customer(dto.firstName(), dto.lastName(), dto.email());
        var saved = repo.save(c);
        registry.counter("demo.customers.created").increment();
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID id) {
        return repo.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Customer> search(String name,
                                 String emailContains,
                                 Instant createdAfter,
                                 Integer page,
                                 Integer size,
                                 String sort) {

        Specification<Customer> spec = Specification.where(null);

        if (name != null && !name.isBlank()) {
            String n = like(name);
            spec = spec.and((root, q, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("firstName")), n),
                            cb.like(cb.lower(root.get("lastName")),  n)
                    )
            );
        }

        if (emailContains != null && !emailContains.isBlank()) {
            String e = like(emailContains);
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("email")), e));
        }

        if (createdAfter != null) {
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdDate"), createdAfter));
        }

        Pageable pageable = buildPageable(page, size, sort);
        return repo.findAll(spec, pageable);
    }

    public Customer update(UUID id, CustomerDto dto) {
        var c = repo.findById(id).orElseThrow(() -> notFound(id));
        c.setFirstName(dto.firstName());
        c.setLastName(dto.lastName());
        c.setEmail(dto.email());
        return repo.save(c);
    }

    public void delete(UUID id) {
        if (!repo.existsById(id)) {
            throw notFound(id);
        }
        repo.deleteById(id);
    }

    private static IdpException notFound(UUID id) {
        ErrorDetail error = ErrorDetail.of(
            ErrorCode.NOT_FOUND,
            "Customer not found.",
            java.util.Map.of("id", String.valueOf(id))
        );
        return new IdpException(error, 404);
    }

    // -- helpers --

    private static String like(String v) {
        return "%" + v.toLowerCase(Locale.ROOT) + "%";
    }

    private static Pageable buildPageable(Integer page, Integer size, String sort) {
        int p = page == null ? 0 : Math.max(page, 0);
        int s = size == null ? 10 : Math.max(size, 1);

        // sort format: "field,dir;field2,dir2" OR "field,dir"
        Sort sortObj = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            Sort combined = null;
            for (String token : sort.split(";")) {
                String[] parts = token.split(",");
                String field = parts[0].trim();
                Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                Sort sPart = Sort.by(dir, field);
                combined = (combined == null) ? sPart : combined.and(sPart);
            }
            if (combined != null) sortObj = combined;
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "createdDate");
        }
        return PageRequest.of(p, s, sortObj);
    }
}
