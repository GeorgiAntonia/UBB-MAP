package domain;

import java.util.Objects;


/**
 * Holds an ordered pair of values.
 *
 * @param <E1> the type of the first value
 * @param <E2> the type of the second value
 */
public class Tuple<E1, E2> {
    private E1 first;
    private E2 second;

    /**
     * Creates a tuple containing the supplied values.
     *
     * @param first the first value
     * @param second the second value
     */
    public Tuple(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Getter for the first value.
     *
     * @return the first value
     */
    public E1 getFirst() {
        return first;
    }

    /**
     * Getter for the second value.
     *
     * @return the second value
     */
    public E2 getSecond() {
        return second;
    }

    /**
     * Creates a hash code from tuple values.
     *
     * @return the tuple's hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    /**
     * Compares this tuple with another by values and their order.
     *
     * @param obj the object to compare
     * @return {true} if {obj} is a tuple with equal values in the same order
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Tuple<?, ?> tuple = (Tuple<?, ?>) obj;
        return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
    }
}
