package repository;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import domain.Validator;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.StreamSupport;

/** Loads and stores friendships in a semicolon-delimited text file. */
public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {
    private final Repository<Long, Utilizator> users;

    /**
     * Creates a file repository for friendships.
     *
     * @param validator the validator applied to friendships
     * @param filename the friendship-data file path
     * @param users the user repository used to migrate legacy name-based records
     */
    public PrietenieFileRepository(
            Validator<Prietenie> validator,
            String filename,
            Repository<Long, Utilizator> users) {
        super(validator, filename, Prietenie::getId, null, false);
        this.users = users;
        loadData();
    }

    /**
     * Parses an ID-based friendship record or migrates a legacy name-based record.
     *
     * @param line the semicolon-delimited friendship record
     * @return the parsed friendship
     * @throws IllegalArgumentException if the record is malformed or refers to an unknown user
     * @throws java.time.format.DateTimeParseException if the timestamp is invalid
     */
    @Override
    protected Prietenie extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length == 3) {
            return new Prietenie(Long.parseLong(parts[0]), Long.parseLong(parts[1]), LocalDateTime.parse(parts[2]));
        }
        if (parts.length != 5) {
            throw new IllegalArgumentException("A friendship must contain three ID fields.");
        }
        return new Prietenie(resolveUserId(parts[0], parts[1]), resolveUserId(parts[2], parts[3]),
                LocalDateTime.parse(parts[4]));
    }

    private Long resolveUserId(String lastName, String firstName) {
        return StreamSupport.stream(users.findAll().spliterator(), false)
                .filter(user -> Objects.equals(user.getNume(), lastName) && Objects.equals(user.getPrenume(), firstName))
                .map(Utilizator::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Friendship refers to an unknown user."));
    }

    /**
     * Formats a friendship as two name-and-surname pairs followed by its timestamp.
     *
     * @param entity the friendship to serialize
     * @return the semicolon-delimited friendship record
     */
    @Override
    protected String createEntityAsString(Prietenie entity) {
        Tuple<Long, Long> id = entity.getId();
        return id.getFirst() + ";" + id.getSecond() + ";" + entity.getData();
    }
}