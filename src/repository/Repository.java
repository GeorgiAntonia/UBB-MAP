package repository;
import domain.Entity;
import exceptions.ValidationException;

/**
 * Defines CRUD operations for entities identified by IDs.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public interface Repository<ID, E extends Entity<ID>> {
    /**
     * Finds an entity by its identifier.
     *
     * @param id the identifier to search for; must not be {null}
     * @return the matching entity, or {null} if no entity has that identifier
     * @throws IllegalArgumentException if {id} is {null}
     */
    E findOne(ID id);


    /**
     * Returns all entities currently held by the repository.
     *
     * @return all entities
     */
    Iterable<E> findAll();


    /**
     * Validates and saves an entity if its identifier is not already present.
     *
     * @param entity the entity to save; must not be {null}
     * @return {null} if saved; otherwise, the entity whose identifier already exists
     * @throws ValidationException if the entity is invalid
     * @throws IllegalArgumentException if {entity} is {null}
     */
    E save(E entity);


    /**
     * Removes an entity by its identifier.
     *
     * @param id the identifier of the entity to remove; must not be {null}
     * @return the removed entity, or {null} if no entity has that identifier
     * @throws IllegalArgumentException if {id} is {null}
     */
    E delete(ID id);


    /**
     * Validates and updates the entity with the same identifier.
     *
     * @param entity the replacement entity; must not be {null}
     * @return {null} if updated; otherwise, the entity because its identifier was not found
     * @throws IllegalArgumentException if {entity} is {null}
     * @throws ValidationException if the entity is invalid
     */
    E update(E entity);
}