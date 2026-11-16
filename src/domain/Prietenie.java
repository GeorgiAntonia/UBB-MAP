package domain;

import java.time.LocalDateTime;


/** Represents a friendship between two user IDs, stored in database-key order. */
public class Prietenie extends Entity<Tuple<Long, Long>> {
    private final LocalDateTime data;

    /**
     * Creates a friendship and records the current date and time.
     *
     * @param user1 the ID of one user
     * @param user2 the ID of the other user
     */
    public Prietenie(Long user1, Long user2) {
        this(user1, user2, LocalDateTime.now());
    }

    /**
     * Creates a friendship with its persisted creation time.
     *
     * @param user1 the ID of one user
     * @param user2 the ID of the other user
     * @param data the friendship creation date and time
     */
    public Prietenie(Long user1, Long user2, LocalDateTime data) {
        if (user1 == null || user2 == null) {
            throw new IllegalArgumentException("Friendship user IDs cannot be null!");
        }
        if (user1.compareTo(user2) <= 0) {
            this.setId(new Tuple<>(user1, user2));
        } else {
            this.setId(new Tuple<>(user2, user1));
        }
        this.data = data;
    }

    /**
     * Returns when this friendship was created.
     *
     * @return the creation date and time
     */
    public LocalDateTime getData() {
        return data;
    }

}
