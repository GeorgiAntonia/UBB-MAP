package repository;

import exceptions.ValidationException;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Defines CRUD operations for entities identified by IDs.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public interface Repository<ID, E> {
    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier to search for; must not be {@code null}
     * @return the matching entity, or {@code null} if no entity has that identifier
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    E findOne(ID id);

    /**
     * Returns all entities currently held by the repository.
     *
     * @return all entities
     */
    Iterable<E> findAll();

    /**
     * Returns entities matching the supplied predicate.
     *
     * @param filter the inclusion rule; must not be {@code null}
     * @return the matching entities
     * @throws IllegalArgumentException if {@code filter} is {@code null}
     */
    default List<E> findAll(Predicate<E> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("The filter cannot be null!");
        }
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Validates and saves an entity if its identifier is not already present.
     *
     * @param entity the entity to save; must not be {@code null}
     * @return {@code null} if saved; otherwise, the entity whose identifier already exists
     * @throws ValidationException if the entity is invalid
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     */
    E save(E entity);

    /**
     * Removes an entity by its identifier.
     *
     * @param id the identifier of the entity to remove; must not be {@code null}
     * @return the removed entity, or {@code null} if no entity has that identifier
     * @throws IllegalArgumentException if {@code id} is {@code null}
     */
    E delete(ID id);

    /**
     * Validates and updates the entity with the same identifier.
     *
     * @param entity the replacement entity; must not be {@code null}
     * @return {@code null} if updated; otherwise, the entity because its identifier was not found
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws ValidationException if the entity is invalid
     */
    E update(E entity);
}