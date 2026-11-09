package repository;

import domain.Utilizator;
import domain.Tuple;
import domain.Validator;

/** Loads and stores users in a semicolon-delimited text file. */
public class UtilizatorFileRepository extends AbstractFileRepository<Tuple<String, String>, Utilizator> {

    /**
     * Creates a file repository for users.
     *
     * @param validator the validator applied to users
     * @param filename the user-data file path
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String filename) {
        super(validator, filename, entity -> new Tuple<>(entity.getNume(), entity.getPrenume()));
    }

    /**
     * Parses a user record containing a first name and a last name, or a legacy
     * record containing an ID followed by those names.
     *
     * @param line the semicolon-delimited user record
     * @return the parsed user
     * @throws IllegalArgumentException if the record has an invalid field count or legacy ID
     */
    @Override
    protected Utilizator extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length == 3) {
            long legacyId = Long.parseLong(parts[0]);
            if (legacyId <= 0) {
                throw new IllegalArgumentException("The legacy ID must be strictly positive.");
            }
            return new Utilizator(parts[2], parts[1]);
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
        return entity.getPrenume() + ";" + entity.getNume();
    }
}