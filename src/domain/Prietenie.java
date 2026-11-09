package domain;

import java.time.LocalDateTime;


/** Represents a friendship between two users, stored in a canonical order. */
public class Prietenie extends Entity<Tuple<Tuple<String, String>, Tuple<String, String>>> {
    LocalDateTime data;

    /**
     * Creates a friendship and records the current date and time.
     *
     * @param user1 the name pair of one user
     * @param user2 the name pair of the other user
     */
    public Prietenie(Tuple<String, String> user1, Tuple<String, String> user2) {
        this(user1, user2, LocalDateTime.now());
    }

    /**
     * Creates a friendship with its persisted creation time.
     *
     * @param user1 the name pair of one user
     * @param user2 the name pair of the other user
     * @param data the friendship creation date and time
     */
    public Prietenie(Tuple<String, String> user1, Tuple<String, String> user2, LocalDateTime data) {
        if (compare(user1, user2) <= 0) {
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

    private int compare(Tuple<String, String> first, Tuple<String, String> second) {
        int compareNume = first.getFirst().compareTo(second.getFirst());
        return compareNume != 0 ? compareNume : first.getSecond().compareTo(second.getSecond());
    }
}
