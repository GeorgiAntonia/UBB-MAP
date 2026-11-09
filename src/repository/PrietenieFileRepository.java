package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Validator;

import java.time.LocalDateTime;

/** Loads and stores friendships in a semicolon-delimited text file. */
public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie> {

    /**
     * Creates a file repository for friendships.
     *
     * @param validator the validator applied to friendships
     * @param filename the friendship-data file path
     */
    public PrietenieFileRepository(Validator<Prietenie> validator, String filename) {
        super(validator, filename, Prietenie::getId);
    }

    /**
     * Parses a friendship record containing both users' names and a timestamp.
     *
     * @param line the semicolon-delimited friendship record
     * @return the parsed friendship
     * @throws IllegalArgumentException if the record does not contain five fields
     * @throws java.time.format.DateTimeParseException if the timestamp is invalid
     */
    @Override
    protected Prietenie extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("A friendship must have exactly 5 fields.");
        }
        Tuple<String, String> user1 = new Tuple<>(parts[0], parts[1]);
        Tuple<String, String> user2 = new Tuple<>(parts[2], parts[3]);
        return new Prietenie(user1, user2, LocalDateTime.parse(parts[4]));
    }

    /**
     * Formats a friendship as two name-and-surname pairs followed by its timestamp.
     *
     * @param entity the friendship to serialize
     * @return the semicolon-delimited friendship record
     */
    @Override
    protected String createEntityAsString(Prietenie entity) {
        Tuple<Tuple<String, String>, Tuple<String, String>> id = entity.getId();
        return id.getFirst().getFirst() + ";" + id.getFirst().getSecond() + ";" +
                id.getSecond().getFirst() + ";" + id.getSecond().getSecond() + ";" + entity.getData();
    }
}