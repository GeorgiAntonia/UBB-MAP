package repository;

import domain.Utilizator;
import domain.Validator;

/** Loads and stores users in a semicolon-delimited text file. */
public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {

    /**
     * Creates a file repository for users.
     *
     * @param validator the validator applied to users
     * @param filename the user-data file path
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String filename) {
        super(validator, filename);
    }

    /**
     * Parses a user record containing an ID, first name, and last name.
     *
     * @param line the semicolon-delimited user record
     * @return the parsed user
     * @throws IllegalArgumentException if the record does not contain three fields or the ID is invalid
     */
    @Override
    protected Utilizator extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Un utilizator trebuie sa aiba exact 3 campuri.");
        }
        Utilizator u = new Utilizator(parts[2], parts[1]); // nume, prenume
        u.setId(Long.parseLong(parts[0]));
        return u;
    }

    /**
     * Formats a user as an ID, first name, and last name.
     *
     * @param entity the user to serialize
     * @return the semicolon-delimited user record
     */
    @Override
    protected String createEntityAsString(Utilizator entity) {
        return entity.getId() + ";" + entity.getPrenume() + ";" + entity.getNume();
    }
}