package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;
import exceptions.RepositoryException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Repository for persisting friendships in the PostgreSQL database. */
public class PrietenieDbRepository implements Repository<Tuple<Long, Long>, Prietenie> {
    private final Validator<Prietenie> validator;
    private final String url;
    private final String user;
    private final String password;

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
     * Finds a friendship by its two user IDs.
     *
     * @param id the two user IDs identifying the friendship
     * @return the matching friendship, or {@code null} if not found
     * @throws IllegalArgumentException if {@code id} or either ID is {@code null}
     * @throws RepositoryException if the database operation fails
     */
    @Override
    public Prietenie findOne(Tuple<Long, Long> id) {
        requireId(id);
        String sql = "SELECT id1, id2, friends_from FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id.getFirst());
            statement.setLong(2, id.getSecond());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readFriendship(result) : null;
            }
        } catch (SQLException e) {
            throw new RepositoryException("Could not find friendship", e);
        }
    }

    /**
     * Returns all friendships.
     *
     * @return all friendships
     * @throws RepositoryException if the database operation fails
     */
    @Override
    public Iterable<Prietenie> findAll() {
        String sql = "SELECT id1, id2, friends_from FROM friendships ORDER BY id1, id2";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            List<Prietenie> friendships = new ArrayList<>();
            while (result.next()) {
                friendships.add(readFriendship(result));
            }
            return friendships;
        } catch (SQLException e) {
            throw new RepositoryException("Could not load friendships", e);
        }
    }

    /**
     * Saves a friendship.
     *
     * @param entity the friendship to save
     * @return the existing friendship if its user IDs are already present; otherwise {@code null}
     * @throws IllegalArgumentException if {@code entity} is {@code null}
     * @throws exceptions.ValidationException if the friendship is invalid
     * @throws RepositoryException if the database operation fails
     */
    @Override
    public Prietenie save(Prietenie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The entity cannot be null!");
        }
        validator.validate(entity);
        Prietenie existing = findOne(entity.getId());
        if (existing != null) {
            return existing;
        }
        String sql = "INSERT INTO friendships (id1, id2, friends_from) VALUES (?, ?, ?)";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, entity);
            statement.executeUpdate();
            return null;
        } catch (SQLException e) {
            throw new RepositoryException("Could not save friendship", e);
        }
    }

    /**
     * Deletes a friendship by its two user IDs.
     *
     * @param id the two user IDs identifying the friendship
     * @return the deleted friendship, or {@code null} if not found
     * @throws IllegalArgumentException if {@code id} or either ID is {@code null}
     * @throws RepositoryException if the database operation fails
     */
    @Override
    public Prietenie delete(Tuple<Long, Long> id) {
        requireId(id);
        Prietenie existing = findOne(id);
        if (existing == null) {
            return null;
        }
        String sql = "DELETE FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id.getFirst());
            statement.setLong(2, id.getSecond());
            return statement.executeUpdate() == 1 ? existing : null;
        } catch (SQLException e) {
            throw new RepositoryException("Could not delete friendship", e);
        }
    }

    /**
     * Updates the timestamp of an existing friendship.
     *
     * @param entity the friendship containing the updated timestamp
     * @return {@code null} if updated; otherwise the supplied friendship
     * @throws IllegalArgumentException if {@code entity} or its ID is {@code null}
     * @throws exceptions.ValidationException if the friendship is invalid
     * @throws RepositoryException if the database operation fails
     */
    @Override
    public Prietenie update(Prietenie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("The entity cannot be null!");
        }
        requireId(entity.getId());
        validator.validate(entity);
        String sql = "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(entity.getData()));
            statement.setLong(2, entity.getId().getFirst());
            statement.setLong(3, entity.getId().getSecond());
            return statement.executeUpdate() == 1 ? null : entity;
        } catch (SQLException e) {
            throw new RepositoryException("Could not update friendship", e);
        }
    }

    private Prietenie readFriendship(ResultSet result) throws SQLException {
        return new Prietenie(result.getLong("id1"), result.getLong("id2"),
                result.getTimestamp("friends_from").toLocalDateTime());
    }

    private void bind(PreparedStatement statement, Prietenie entity) throws SQLException {
        statement.setLong(1, entity.getId().getFirst());
        statement.setLong(2, entity.getId().getSecond());
        statement.setTimestamp(3, Timestamp.valueOf(entity.getData()));
    }

    private void requireId(Tuple<Long, Long> id) {
        if (id == null || id.getFirst() == null || id.getSecond() == null) {
            throw new IllegalArgumentException("The friendship IDs cannot be null!");
        }
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

}
