package service;

import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import repository.Repository;

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
            throw new IllegalArgumentException("Repository-ul si serviciul nu pot fi null!");
        }
        this.utilizatorRepository = utilizatorRepository;
        this.prietenieService = prietenieService;
    }

    /**
     * Adds a user to the network.
     *
     * @param utilizator the user to add
     * @throws IllegalArgumentException if {@code utilizator} is {@code null}
     * @throws ServiceException if a user with the same ID already exists
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
     * Removes a user from the network.
     *
     * @param id the ID of the user to remove
     * @return the removed user, or {@code null} if the ID is not present
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public Utilizator delUtilizator(Long id) {
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
