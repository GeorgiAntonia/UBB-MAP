package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;

import java.time.LocalDateTime;

/** Loads and stores friendships in a semicolon-delimited text file. */
public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {

    /**
     * Creates a file repository for friendships.
     *
     * @param validator the validator applied to friendships
     * @param filename the friendship-data file path
     */
    public PrietenieFileRepository(Validator<Prietenie> validator, String filename) {
        super(validator, filename);
    }

    /**
     * Parses a friendship record containing two user IDs and a timestamp.
     *
     * @param line the semicolon-delimited friendship record
     * @return the parsed friendship
     * @throws IllegalArgumentException if the record does not contain three fields or an ID is invalid
     * @throws java.time.format.DateTimeParseException if the timestamp is invalid
     */
    @Override
    protected Prietenie extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("O prietenie trebuie sa aiba exact 3 campuri.");
        }
        return new Prietenie(Long.parseLong(parts[0]), Long.parseLong(parts[1]), LocalDateTime.parse(parts[2]));
    }

    /**
     * Formats a friendship as two user IDs followed by its timestamp.
     *
     * @param entity the friendship to serialize
     * @return the semicolon-delimited friendship record
     */
    @Override
    protected String createEntityAsString(Prietenie entity) {
        return entity.getId().getFirst() + ";" + entity.getId().getSecond() + ";" + entity.getData().toString();
    }
}