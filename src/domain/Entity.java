package domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Base class for domain objects identified by an ID.
 *
 * @param <ID> the identifier type
 */
public class Entity<ID> implements Serializable {

    private ID id;

    /**
     * Returns this entity's ID.
     *
     * @return the identifier, or {@code null} if none has been assigned
     */
    public ID getId() {
        return id;
    }

    /**
     * Assigns this entity's ID.
     *
     * @param id the ID to assign
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Compares this entity with another entity by ID.
     *
     * @param o the object to compare
     * @return {@code true} if both objects are entities with equal IDs
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Entity<?>)) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(getId(), entity.getId());
    }

    /**
     * Computes a hash code from this entity's ID.
     *
     * @return the identifier-based hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
