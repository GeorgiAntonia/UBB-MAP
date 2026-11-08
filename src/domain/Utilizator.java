package domain;

import java.util.ArrayList;
import java.util.List;
/** Represents a user in the social network and maintains the user's friends. */
public class Utilizator extends Entity<Long> {
    private String nume;
    private String prenume;
    private List<Utilizator> prieteni;
    /**
     * Creates a user with no friends.
     *
     * @param nume the user's nume
     * @param prenume the user's prenume
     */
    public Utilizator(String nume, String prenume) {
        this.nume = nume;
        this.prenume = prenume;
        this.prieteni = new ArrayList<>();
    }

    /**
     * Returns the user's nume.
     *
     * @return the nume
     */
    public String getNume() {
        return nume;
    }

    /**
     * Sets the user's nume.
     *
     * @param nume the new nume
     */
    public void setNume(String nume) {
        this.nume = nume;
    }

    /**
     * Returns the user's prenume.
     *
     * @return the prenume
     */
    public String getPrenume() {
        return prenume;
    }

    /**
     * Sets the user's prenume.
     *
     * @param prenume the new prenume
     */
    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }

    /**
     * Returns the list of this user's friends.
     *
     * @return the friend list
     */
    public List<Utilizator> getPrieteni() {
        return prieteni;
    }

    /**
     * Adds a user to this user's friend list.
     *
     * @param prieten the user to add
     */
    public void addPrieten(Utilizator prieten) {
        this.prieteni.add(prieten);
    }

    /**
     * Removes the matching user from this user's friend list.
     *
     * @param prieten the user to remove
     */
    public void delPrieten(Utilizator prieten) {
        this.prieteni.remove(prieten);
    }

    /**
     * Compares users by their inherited entity identifiers.
     *
     * @param o the object to compare
     * @return {@code true} if {@code o} is an entity with the same identifier
     */
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    /**
     * Computes a hash code from the inherited entity identifier.
     *
     * @return the user's hash code
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns a readable representation of this user.
     *
     * @return the user details as a string
     */
    @Override
    public String toString() {
        return "Utilizator{" +
                "id='" + getId() + '\'' +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                '}';
    }
}
