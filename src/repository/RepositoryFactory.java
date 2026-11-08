package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import domain.Validator;
import exceptions.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Creates repositories according to the persistence configuration.
 */
public class RepositoryFactory {
    private final Properties properties;
    private final RepositoryType repositoryType;

    /**
     * Loads {@code config.properties} from the application classpath.
     *
     * @throws RepositoryException if the configuration is missing, unreadable, or invalid
     */
    public RepositoryFactory() {
        properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RepositoryException("File config.properties not found in classpath!");
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RepositoryException("Error loading configuration properties", e);
        }
        repositoryType = readRepositoryType();
    }

    /**
     * Creates the configured user repository.
     *
     * @param validator the validator applied to users
     * @return the configured user repository
     * @throws RepositoryException if a required configuration value is missing
     */
    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        switch (repositoryType) {
            case MEMORY:
                return new InMemoryRepository<>(validator);
            case FILE:
                return new UtilizatorFileRepository(validator, requiredProperty("utilizator.file"));
            case DATABASE:
                return new UtilizatorDbRepository(
                        validator,
                        requiredProperty("db.url"),
                        requiredProperty("db.user"),
                        requiredProperty("db.password")
                );
            default:
                throw new RepositoryException("Unknown repository type: " + repositoryType);
        }
    }

    /**
     * Creates the configured friendship repository.
     *
     * @param validator the validator applied to friendships
     * @return the configured friendship repository
     * @throws RepositoryException if a required configuration value is missing
     */
    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        switch (repositoryType) {
            case MEMORY:
                return new InMemoryRepository<>(validator);
            case FILE:
                return new PrietenieFileRepository(validator, requiredProperty("prietenie.file"));
            case DATABASE:
                return new PrietenieDbRepository(
                        validator,
                        requiredProperty("db.url"),
                        requiredProperty("db.user"),
                        requiredProperty("db.password")
                );
            default:
                throw new RepositoryException("Unknown repository type: " + repositoryType);
        }
    }

    /**
     * Rebuilds the in-memory user friendship lists from persisted friendship records.
     * Records referring to a missing user are removed from the friendship repository.
     *
     * @param users the user repository
     * @param friendships the friendship repository
     */
    public void initializeFriendships(
            Repository<Long, Utilizator> users,
            Repository<Tuple<Long, Long>, Prietenie> friendships) {
        if (repositoryType != RepositoryType.FILE) {
            return;
        }

        List<Tuple<Long, Long>> invalidFriendships = new ArrayList<>();
        for (Prietenie friendship : friendships.findAll()) {
            Long firstId = friendship.getId().getFirst();
            Long secondId = friendship.getId().getSecond();
            Utilizator first = users.findOne(firstId);
            Utilizator second = users.findOne(secondId);
            if (first == null || second == null) {
                invalidFriendships.add(friendship.getId());
            } else {
                first.addPrieten(second);
                second.addPrieten(first);
            }
        }
        for (Tuple<Long, Long> id : invalidFriendships) {
            friendships.delete(id);
        }
    }

    /**
     * Reads and parses the configured persistence strategy.
     *
     * @return the selected repository type
     * @throws RepositoryException if the type is missing or not recognized
     */
    private RepositoryType readRepositoryType() {
        String type = properties.getProperty("repository.type");
        if (type == null || type.trim().isEmpty()) {
            throw new RepositoryException("Property repository.type is missing!");
        }
        try {
            return RepositoryType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Unknown repository type: " + type, e);
        }
    }

    /**
     * Reads a required non-blank configuration property.
     *
     * @param key the property name
     * @return the trimmed property value
     * @throws RepositoryException if the property is missing or blank
     */
    private String requiredProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RepositoryException("Property " + key + " is missing!");
        }
        return value.trim();
    }
}
