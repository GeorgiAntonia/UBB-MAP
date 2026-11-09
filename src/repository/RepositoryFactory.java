package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import domain.Validator;
import exceptions.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Creates repositories according to the persistence configuration.
 */
public class RepositoryFactory {
    private final Properties properties;
    private final RepositoryType repositoryType;
    private final Map<RepositoryType, Function<Validator<Utilizator>, Repository<Tuple<String, String>, Utilizator>>> userRepositories;
    private final Map<RepositoryType, Function<Validator<Prietenie>, Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie>>> friendshipRepositories;

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
        userRepositories = new HashMap<>();
        userRepositories.put(RepositoryType.MEMORY,
                validator -> new InMemoryRepository<>(validator, entity -> new Tuple<>(entity.getNume(), entity.getPrenume())));
        userRepositories.put(RepositoryType.FILE,
                validator -> new UtilizatorFileRepository(validator, requiredProperty("utilizator.file")));
        userRepositories.put(RepositoryType.DATABASE,
                validator -> new UtilizatorDbRepository(
                        validator,
                        requiredProperty("db.url"),
                        requiredProperty("db.user"),
                        requiredProperty("db.password")));

        friendshipRepositories = new HashMap<>();
        friendshipRepositories.put(RepositoryType.MEMORY,
                validator -> new InMemoryRepository<>(validator, Prietenie::getId));
        friendshipRepositories.put(RepositoryType.FILE,
                validator -> new PrietenieFileRepository(validator, requiredProperty("prietenie.file")));
        friendshipRepositories.put(RepositoryType.DATABASE,
                validator -> new PrietenieDbRepository(
                        validator,
                        requiredProperty("db.url"),
                        requiredProperty("db.user"),
                        requiredProperty("db.password")));
    }

    /**
     * Creates the configured user repository.
     *
     * @param validator the validator applied to users
     * @return the configured user repository
     * @throws RepositoryException if a required configuration value is missing
     */
    public Repository<Tuple<String, String>, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        Function<Validator<Utilizator>, Repository<Tuple<String, String>, Utilizator>> factory = userRepositories.get(repositoryType);
        if (factory == null) {
            throw new RepositoryException("Unknown repository type: " + repositoryType);
        }
        return factory.apply(validator);
    }

    /**
     * Creates the configured friendship repository.
     *
     * @param validator the validator applied to friendships
     * @return the configured friendship repository
     * @throws RepositoryException if a required configuration value is missing
     */
    public Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        Function<Validator<Prietenie>, Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie>> factory = friendshipRepositories.get(repositoryType);
        if (factory == null) {
            throw new RepositoryException("Unknown repository type: " + repositoryType);
        }
        return factory.apply(validator);
    }

    /**
     * Rebuilds the in-memory user friendship lists from persisted friendship records.
     * Records referring to a missing user are removed from the friendship repository.
     *
     * @param users the user repository
     * @param friendships the friendship repository
     */
    public void initializeFriendships(
            Repository<Tuple<String, String>, Utilizator> users,
            Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie> friendships) {
        if (repositoryType != RepositoryType.FILE) {
            return;
        }

        List<Tuple<Tuple<String, String>, Tuple<String, String>>> invalidFriendships = StreamSupport.stream(friendships.findAll().spliterator(), false)
                .map(Prietenie::getId)
                .filter(id -> users.findOne(id.getFirst()) == null || users.findOne(id.getSecond()) == null)
                .collect(Collectors.toList());

        invalidFriendships.forEach(friendships::delete);
        StreamSupport.stream(friendships.findAll().spliterator(), false)
                .map(Prietenie::getId)
                .forEach(id -> {
                    Utilizator first = users.findOne(id.getFirst());
                    Utilizator second = users.findOne(id.getSecond());
                    if (first != null && second != null) {
                        first.addPrieten(second);
                        second.addPrieten(first);
                    }
                });
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
