package service;

import domain.Tuple;
import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import repository.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Provides operations for managing users in the social network.
 */
public class UtilizatorService {
    private final Repository<Long, Utilizator> utilizatorRepository;
    private final PrietenieService prietenieService;

    /**
     * Creates a user service backed by the supplied repository.
     *
     * @param utilizatorRepository the repository used to store users
     * @param prietenieService the service used to remove friendships before deleting a user
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public UtilizatorService(
            Repository<Long, Utilizator> utilizatorRepository,
            PrietenieService prietenieService) {
        if (utilizatorRepository == null || prietenieService == null) {
            throw new IllegalArgumentException("The repository and service cannot be null!");
        }
        this.utilizatorRepository = utilizatorRepository;
        this.prietenieService = prietenieService;
    }

    /**
     * Adds a user to the network.
     *
     * @param utilizator the user to add
     * @throws IllegalArgumentException if {@code utilizator} is {@code null}
     * @throws ServiceException if a user with the same name and surname already exists
     * @throws ValidationException if the user is invalid
     * @throws RepositoryException if persistence fails
     */
    public void addUtilizator(Utilizator utilizator) {
        if (utilizator == null) {
            throw new IllegalArgumentException("The user cannot be null!");
        }
        if (utilizator.getNume() != null && utilizator.getPrenume() != null
                && findByName(new Tuple<>(utilizator.getNume(), utilizator.getPrenume())) != null) {
            throw new ServiceException("Utilizatorul exista deja!");
        }
        if (utilizatorRepository.save(utilizator) != null) {
            throw new ServiceException("Utilizatorul exista deja!");
        }
    }

    /**
     * Finds a user by name and surname.
     *
     * @param name the user's last-name and first-name pair
     * @return the matching user wrapped in an Optional
     */
    public Optional<Utilizator> findUtilizator(Tuple<String, String> name) {
        return Optional.ofNullable(findByName(name));
    }

    /**
     * Finds a user by database identifier.
     *
     * @param id the user identifier
     * @return the matching user wrapped in an Optional
     */
    public Optional<Utilizator> findUtilizatorById(Long id) {
        return Optional.ofNullable(utilizatorRepository.findOne(id));
    }

    /**
     * Removes a user from the network.
     *
     * @param id the user identifier
     * @return the removed user, or {@code null} if the user is not present
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if persistence fails
     */
    public Utilizator delUtilizator(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The user ID cannot be null!");
        }
        Utilizator user = utilizatorRepository.findOne(id);
        if (user == null) {
            return null;
        }
        prietenieService.delPrieteniiUtilizator(id);
        return utilizatorRepository.delete(id);
    }

    /**
     * Returns all users in the network.
     *
     * @return all users
     */
    public Iterable<Utilizator> getAllUtilizatori() {
        return utilizatorRepository.findAll();
    }

    private Utilizator findByName(Tuple<String, String> name) {
        if (name == null || name.getFirst() == null || name.getSecond() == null) {
            throw new IllegalArgumentException("The user name cannot be null!");
        }
        return StreamSupport.stream(utilizatorRepository.findAll().spliterator(), false)
                .filter(user -> Objects.equals(user.getNume(), name.getFirst())
                        && Objects.equals(user.getPrenume(), name.getSecond()))
                .findFirst()
                .orElse(null);
    }
}
