package repository;

import domain.Entity;
import exceptions.ValidationException;
import domain.Validator;

import java.util.HashMap;
import java.util.Map;


/**
 * Stores validated entities in a hash map in memory.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private Validator<E> validator;
    Map<ID, E> entities;

    /**
     * Creates an empty repository using the supplied validator.
     *
     * @param validator the validator used
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        entities = new HashMap<>();
    }

    /**
     * Finds an entity by ID.
     *
     * @param id the ID to search for
     * @return the matching entity, or {null} if no entity has that ID
     * @throws IllegalArgumentException if {id} is {null}
     */
    @Override
    public E findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null!");
        }
        return entities.get(id);
    }

    /**
     * Returns a view of all stored entities.
     *
     * @return all stored entities
     */
    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    /**
     * Validates and stores an entity if its ID is not already present.
     *
     * @param entity the entity to store
     * @return {null} if stored; otherwise, the entity whose ID already exists
     * @throws IllegalArgumentException if {entity} is {null}
     * @throws ValidationException if the entity fails validation
     */
    @Override
    public E save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) {
            return entities.get(entity.getId());
        }
        entities.put(entity.getId(), entity);
        return null;
    }

    /**
     * Removes an entity by ID.
     *
     * @param id the ID of the entity to remove
     * @return the removed entity, or {null} if no entity has that ID
     * @throws IllegalArgumentException if {id} is {null}
     */
    @Override
    public E delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null!");
        }
        return entities.remove(id);
    }

    /**
     * Validates and replaces the entity with the same ID.
     *
     * @param entity the replacement entity
     * @return {null} if updated; otherwise, the entity because its ID was not found
     * @throws IllegalArgumentException if {entity} is {null}
     * @throws ValidationException if the entity fails validation
     */
    @Override
    public E update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);
        if (entities.containsKey(entity.getId())) {
            entities.put(entity.getId(), entity);
            return null;
        }
        return entity;
    }
}