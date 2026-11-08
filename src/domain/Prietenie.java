package domain;

import java.time.LocalDateTime;


/** Represents a friendship between two users, stored with IDs in ascending order. */
public class Prietenie extends Entity<Tuple<Long, Long>>{
    LocalDateTime data;

    /**
     * Creates a friendship and records the current date and time.
     *
     * @param user1 the ID of one user
     * @param user2 the ID of the other user
     */
    public Prietenie(Long user1, Long user2) {
        Long id1 = Math.min(user1, user2);
        Long id2 = Math.max(user1, user2);
        this.setId(new Tuple<>(id1, id2));
        this.data = LocalDateTime.now();
    }

    /**
     * Creates a friendship with its persisted creation time.
     *
     * @param user1 the ID of one user
     * @param user2 the ID of the other user
     * @param data the friendship creation date and time
     */
    public Prietenie(Long user1, Long user2, LocalDateTime data) {
        Long id1 = Math.min(user1, user2);
        Long id2 = Math.max(user1, user2);
        this.setId(new Tuple<>(id1, id2));
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
