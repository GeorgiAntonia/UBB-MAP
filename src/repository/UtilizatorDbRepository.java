package repository;

import domain.Utilizator;
import domain.Validator;

/** Repository for persisting users in a database. */
public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private Validator<Utilizator> validator;
    private String url, user, password;

    /**
     * Creates a user repository configured for a database connection.
     *
     * @param validator the validator applied to users
     * @param url the database connection URL
     * @param user the database user name
     * @param password the database user's password
     */
    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Finds a user by ID.
     *
     * @param id the user ID to search for
     * @return the matching user
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Utilizator findOne(Long id) { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Returns all users.
     *
     * @return all users
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Iterable<Utilizator> findAll() { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Saves a user.
     *
     * @param entity the user to save
     * @return the existing user if its ID is already present; otherwise {@code null}
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Utilizator save(Utilizator entity) { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Deletes a user by ID.
     *
     * @param id the ID of the user to delete
     * @return the deleted user, or {@code null} if no user has that ID
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Utilizator delete(Long id) { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Updates an existing user.
     *
     * @param entity the user containing the updated data
     * @return {@code null} if updated; otherwise the supplied user
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Utilizator update(Utilizator entity) { throw new UnsupportedOperationException("JDBC not implemented yet"); }
}