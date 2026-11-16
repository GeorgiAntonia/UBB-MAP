package repository;

import domain.Utilizator;
import domain.Tuple;
import domain.Validator;

import java.util.concurrent.atomic.AtomicLong;

/** Loads and stores users in a semicolon-delimited text file. */
public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {
    private final AtomicLong lastId;

    /**
     * Creates a file repository for users.
     *
     * @param validator the validator applied to users
     * @param filename the user-data file path
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String filename) {
        super(validator, filename, Utilizator::getId, null, false);
        lastId = new AtomicLong();
        loadData();
    }

    @Override
    protected void assignIdIfMissing(Utilizator entity) {
        if (entity.getId() == null) {
            entity.setId(lastId.incrementAndGet());
        }
    }

    /**
     * Parses a user record containing an ID, first name, and last name, or a legacy
     * record containing only the first and last names.
     *
     * @param line the semicolon-delimited user record
     * @return the parsed user
     * @throws IllegalArgumentException if the record has an invalid field count or ID
     */
    @Override
    protected Utilizator extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length == 3) {
            long id = Long.parseLong(parts[0]);
            if (id <= 0) {
                throw new IllegalArgumentException("The user ID must be strictly positive.");
            }
            lastId.accumulateAndGet(id, Math::max);
            Utilizator user = new Utilizator(parts[2], parts[1]);
            user.setId(id);
            return user;
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("A user must have exactly 2 fields.");
        }
        return new Utilizator(parts[1], parts[0]); // nume, prenume
    }

    /**
     * Formats a user as a first name and a last name.
     *
     * @param entity the user to serialize
     * @return the semicolon-delimited user record
     */
    @Override
    protected String createEntityAsString(Utilizator entity) {
        return entity.getId() + ";" + entity.getPrenume() + ";" + entity.getNume();
    }

}