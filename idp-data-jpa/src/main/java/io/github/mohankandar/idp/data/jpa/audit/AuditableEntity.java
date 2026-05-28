package io.github.mohankandar.idp.data.jpa.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Reusable audit fields. Does NOT impose an ID type or strategy.
 * Extend this in your @Entity and declare your own @Id field(s).
 */
@MappedSuperclass
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    protected Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    protected Instant updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 128, updatable = false)
    protected String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 128)
    protected String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    protected long version;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public long getVersion() {
        return version;
    }
}
