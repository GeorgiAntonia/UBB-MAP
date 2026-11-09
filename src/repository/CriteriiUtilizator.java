package repository;

import domain.Utilizator;

import java.util.function.Predicate;

/**
 * Static filters for user collections.
 */
public final class CriteriiUtilizator {
    private CriteriiUtilizator() {
    }

    /**
     * Returns users who have no friends.
     *
     * @return predicate selecting isolated users
     */
    public static Predicate<Utilizator> faraPrieteni() {
        return user -> user.getPrieteni().isEmpty();
    }

    /**
     * Returns users who have at least one friend.
     *
     * @return predicate selecting users with friends
     */
    public static Predicate<Utilizator> arePrieteni() {
        return Predicate.not(faraPrieteni());
    }

    /**
     * Returns users whose last name matches the given value.
     *
     * @param nume the last name to match
     * @return predicate for users with that last name
     */
    public static Predicate<Utilizator> dupaNume(String nume) {
        return user -> user.getNume().equalsIgnoreCase(nume);
    }

    /**
     * Returns users whose first name matches the given value.
     *
     * @param prenume the first name to match
     * @return predicate for users with that first name
     */
    public static Predicate<Utilizator> dupaPrenume(String prenume) {
        return user -> user.getPrenume().equalsIgnoreCase(prenume);
    }

    /**
     * Returns users whose last name starts with the given prefix.
     *
     * @param prefix the prefix to test
     * @return predicate for users whose last name starts with the prefix
     */
    public static Predicate<Utilizator> numeIncepeCu(String prefix) {
        return user -> user.getNume() != null && user.getNume().startsWith(prefix);
    }
}
