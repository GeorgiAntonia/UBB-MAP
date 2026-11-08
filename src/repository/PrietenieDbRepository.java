package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;

/** Repository for persisting friendships in a database. */
public class PrietenieDbRepository implements Repository<Tuple<Long, Long>, Prietenie> {
    private Validator<Prietenie> validator;
    private String url, user, password;

    /**
     * Creates a friendship repository configured for a database connection.
     *
     * @param validator the validator applied to friendships
     * @param url the database connection URL
     * @param user the database user name
     * @param password the database user's password
     */
    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Finds a friendship by its user-ID pair.
     *
     * @param id the user-ID pair identifying the friendship
     * @return the matching friendship
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Prietenie findOne(Tuple<Long, Long> id) { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Returns all friendships.
     *
     * @return all friendships
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Iterable<Prietenie> findAll() { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Saves a friendship.
     *
     * @param entity the friendship to save
     * @return the existing friendship if its ID is already present; otherwise {@code null}
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Prietenie save(Prietenie entity) { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Deletes a friendship by its user-ID pair.
     *
     * @param id the user-ID pair identifying the friendship
     * @return the deleted friendship, or {@code null} if none has that ID
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Prietenie delete(Tuple<Long, Long> id) { throw new UnsupportedOperationException("JDBC not implemented yet"); }

    /**
     * Updates an existing friendship.
     *
     * @param entity the friendship containing the updated data
     * @return {@code null} if updated; otherwise the supplied friendship
     * @throws UnsupportedOperationException because JDBC persistence is implemented in Lab 4
     */
    @Override
    public Prietenie update(Prietenie entity) { throw new UnsupportedOperationException("JDBC not implemented yet"); }
}