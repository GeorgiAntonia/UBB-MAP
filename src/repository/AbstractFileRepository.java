package repository;

import domain.Entity;
import domain.Validator;
import exceptions.RepositoryException;
import exceptions.ValidationException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeParseException;

/**
 * Provides file loading and persistence for validated entities.
 *
 * @param <ID> the identifier type
 * @param <E> the entity type
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private final String filename;

    /**
     * Creates a repository and loads its initial contents from a text file.
     *
     * @param validator the validator applied to loaded and saved entities
     * @param filename the relative or absolute file path
     */
    public AbstractFileRepository(Validator<E> validator, String filename) {
        super(validator);
        this.filename = filename;
        loadData();
    }

    /**
     * Parses one file line into an entity.
     *
     * @param line the serialized entity
     * @return the parsed entity
     */
    protected abstract E extractEntity(String line);

    /**
     * Serializes one entity into a file line.
     *
     * @param entity the entity to serialize
     * @return the serialized entity
     */
    protected abstract String createEntityAsString(E entity);

    /** Loads valid entities from the configured file, skipping malformed records. */
    private void loadData() {
        Path path = Paths.get(filename);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    E entity = extractEntity(line);
                    E duplicate = super.save(entity);
                    if (duplicate != null) {
                        System.err.println("Linie corupta ignorata in " + filename + " la linia " + lineNumber + ": ID duplicat.");
                    }
                } catch (IllegalArgumentException | DateTimeParseException | ValidationException e) {
                    System.err.println("Linie corupta ignorata in " + filename + " la linia " + lineNumber + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Eroare la citirea fisierului " + filename, e);
        }
    }

    /** Writes all current entities to the configured file. */
    private void writeToFile() {
        Path path = Paths.get(filename);
        Path parent = path.getParent();
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new RepositoryException("Eroare la crearea directorului pentru " + filename, e);
        }

        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (E entity : findAll()) {
                bw.write(createEntityAsString(entity));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RepositoryException("Eroare la scrierea in fisierul " + filename, e);
        }
    }

    /**
     * Validates and stores an entity, then persists the updated collection.
     *
     * @param entity the entity to store
     * @return {@code null} if stored; otherwise, the entity whose ID already exists
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws ValidationException if the entity fails validation
     * @throws RepositoryException if writing the file fails
     */
    @Override
    public E save(E entity) {
        E result = super.save(entity);
        if (result == null) writeToFile();
        return result;
    }

    /**
     * Removes an entity by ID and persists the updated collection.
     *
     * @param id the ID of the entity to remove
     * @return the removed entity, or {@code null} if no entity has that ID
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if writing the file fails
     */
    @Override
    public E delete(ID id) {
        E result = super.delete(id);
        if (result != null) writeToFile();
        return result;
    }

    /**
     * Validates and replaces an entity, then persists the updated collection.
     *
     * @param entity the replacement entity
     * @return {@code null} if updated; otherwise, the entity because its ID was not found
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws ValidationException if the entity fails validation
     * @throws RepositoryException if writing the file fails
     */
    @Override
    public E update(E entity) {
        E result = super.update(entity);
        if (result == null) writeToFile();
        return result;
    }
}