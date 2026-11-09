package service;

import domain.Tuple;
import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import repository.Repository;

import java.util.Optional;

/**
 * Provides operations for managing users in the social network.
 */
public class UtilizatorService {
    private final Repository<Tuple<String, String>, Utilizator> utilizatorRepository;
    private final PrietenieService prietenieService;

    /**
     * Creates a user service backed by the supplied repository.
     *
     * @param utilizatorRepository the repository used to store users
     * @param prietenieService the service used to remove friendships before deleting a user
     * @throws IllegalArgumentException if either argument is {@code null}
     */
    public UtilizatorService(
            Repository<Tuple<String, String>, Utilizator> utilizatorRepository,
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
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public void addUtilizator(Utilizator utilizator) {
        if (utilizatorRepository.save(utilizator) != null) {
            throw new ServiceException("Utilizatorul exista deja!");
        }
    }

    /**
     * Finds a user by identifier.
     *
     * @param id the user key to search for
     * @return the matching user wrapped in an Optional
     */
    public Optional<Utilizator> findUtilizator(Tuple<String, String> id) {
        return Optional.ofNullable(utilizatorRepository.findOne(id));
    }

    /**
     * Removes a user from the network.
     *
     * @param id the name and surname identifying the user to remove
     * @return the removed user, or {@code null} if the user is not present
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public Utilizator delUtilizator(Tuple<String, String> id) {
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
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public Iterable<Utilizator> getAllUtilizatori() {
        return utilizatorRepository.findAll();
    }
}
