package service;

import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import exceptions.RepositoryException;
import exceptions.ServiceException;
import exceptions.ValidationException;
import repository.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Provides operations for managing friendships and friendship communities.
 */
public class PrietenieService {
    private final Repository<Long, Utilizator> utilizatorRepository;
    private final Repository<Tuple<Long, Long>, Prietenie> prietenieRepository;

    /**
     * Creates a friendship service backed by the supplied repositories.
     *
     * @param utilizatorRepository the repository used to find users
     * @param prietenieRepository the repository used to store friendships
     * @throws IllegalArgumentException if either repository is {@code null}
     */
    public PrietenieService(
            Repository<Long, Utilizator> utilizatorRepository,
            Repository<Tuple<Long, Long>, Prietenie> prietenieRepository) {
        if (utilizatorRepository == null || prietenieRepository == null) {
            throw new IllegalArgumentException("The repositories cannot be null!");
        }
        this.utilizatorRepository = utilizatorRepository;
        this.prietenieRepository = prietenieRepository;
    }

    /**
     * Creates a friendship between the users identified by their IDs.
     *
     * @param id1 the ID of one user
     * @param id2 the ID of the other user
     * @throws IllegalArgumentException if either ID is {@code null}
     * @throws ServiceException if either user is absent or the friendship already exists
     * @throws ValidationException if both user IDs are equal or the friendship is invalid
     * @throws RepositoryException if persistence fails
     */
    public void addPrietenie(Long id1, Long id2) {
        Tuple<Long, Long> friendshipId = normalizedId(id1, id2);
        Utilizator first = utilizatorRepository.findOne(id1);
        Utilizator second = utilizatorRepository.findOne(id2);
        if (first == null || second == null) {
            throw new ServiceException("The users do not exist!");
        }
        if (id1.equals(id2)) {
            throw new ValidationException("A user cannot be friends with themselves!");
        }

        if (prietenieRepository.findOne(friendshipId) != null) {
            throw new ServiceException("The friendship already exists!");
        }
        if (prietenieRepository.save(new Prietenie(id1, id2)) != null) {
            throw new ServiceException("The friendship already exists!");
        }

        first.addPrieten(second);
        second.addPrieten(first);
    }

    /**
     * Removes the friendship identified by the two user IDs if it exists.
     *
     * @param id1 the ID of one user
     * @param id2 the ID of the other user
     * @return the removed friendship, or {@code null} if it did not exist
     * @throws IllegalArgumentException if either ID is {@code null}
     * @throws RepositoryException if persistence fails
     */
    public Prietenie delPrietenie(Long id1, Long id2) {
        Tuple<Long, Long> friendshipId = normalizedId(id1, id2);
        Prietenie deleted = prietenieRepository.delete(friendshipId);
        if (deleted != null) {
            Utilizator first = utilizatorRepository.findOne(friendshipId.getFirst());
            Utilizator second = utilizatorRepository.findOne(friendshipId.getSecond());
            if (first != null && second != null) {
                first.delPrieten(second);
                second.delPrieten(first);
            }
        }
        return deleted;
    }

    /**
     * Removes every friendship belonging to the specified user.
     *
     * @param id the database identifier of the user
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if persistence fails
     */
    public void delPrieteniiUtilizator(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("The user ID cannot be null!");
        }
        List<Tuple<Long, Long>> friendshipIds =
                StreamSupport.stream(prietenieRepository.findAll().spliterator(), false)
                        .map(Prietenie::getId)
                        .filter(friendshipId -> id.equals(friendshipId.getFirst()) || id.equals(friendshipId.getSecond()))
                        .collect(Collectors.toList());
        friendshipIds.forEach(friendshipId -> {
            if (prietenieRepository.delete(friendshipId) != null) {
                Utilizator first = utilizatorRepository.findOne(friendshipId.getFirst());
                Utilizator second = utilizatorRepository.findOne(friendshipId.getSecond());
                if (first != null && second != null) {
                    first.delPrieten(second);
                    second.delPrieten(first);
                }
            }
        });
        Utilizator user = utilizatorRepository.findOne(id);
        if (user != null) {
            user.getPrieteni().clear();
        }
    }

    /**
     * Returns all friendships in the network.
     *
     * @return all friendships
     */
    public Iterable<Prietenie> getAllPrietenie() {
        return prietenieRepository.findAll();
    }

    /**
     * Counts the connected components in the friendship graph.
     *
     * @return the number of communities; isolated users each count as one community
     */
    public int getNrComunitati() {
        Set<Long> visited = new HashSet<>();
        return (int) StreamSupport.stream(utilizatorRepository.findAll().spliterator(), false)
                .filter(user -> !visited.contains(user.getId()))
                .peek(user -> visitCommunity(user, visited))
                .count();
    }

    /**
     * Finds the community containing the longest simple friendship path.
     *
     * @return name-and-surname pairs of users in that community, or an empty list if the network is empty
     */
    public List<Tuple<String, String>> getMostSociabilaComunitate() {
        Set<Long> visited = new HashSet<>();
        List<Tuple<String, String>> longestCommunity = new ArrayList<>();
        List<Integer> longestPath = new ArrayList<>();

        StreamSupport.stream(utilizatorRepository.findAll().spliterator(), false)
                .filter(user -> !visited.contains(user.getId()))
                .forEach(user -> {
                    List<Utilizator> component = new ArrayList<>();
                    collectCommunity(user, visited, component);
                    int componentLongestPath = findLongestPathInComponent(component);
                    if (longestPath.isEmpty() || componentLongestPath > longestPath.get(0)) {
                        longestPath.clear();
                        longestPath.add(componentLongestPath);
                        longestCommunity.clear();
                        longestCommunity.addAll(component.stream()
                                .map(this::userName)
                                .collect(Collectors.toList()));
                    }
                });
        return longestCommunity;
    }

    private Tuple<Long, Long> normalizedId(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            throw new IllegalArgumentException("The user IDs cannot be null!");
        }
        return id1 <= id2 ? new Tuple<>(id1, id2) : new Tuple<>(id2, id1);
    }

    private void visitCommunity(Utilizator user, Set<Long> visited) {
        visited.add(user.getId());
        user.getPrieteni().stream()
                .filter(friend -> !visited.contains(friend.getId()))
                .forEach(friend -> visitCommunity(friend, visited));
    }

    private void collectCommunity(Utilizator user, Set<Long> visited, List<Utilizator> component) {
        visited.add(user.getId());
        component.add(user);
        user.getPrieteni().stream()
                .filter(friend -> !visited.contains(friend.getId()))
                .forEach(friend -> collectCommunity(friend, visited, component));
    }

    private int findLongestPathInComponent(List<Utilizator> component) {
        return component.stream()
                .mapToInt(start -> findLongestPath(start, new HashSet<>()))
                .max()
                .orElse(0);
    }

    private int findLongestPath(Utilizator user, Set<Long> pathVisited) {
        pathVisited.add(user.getId());
        int longestPath = user.getPrieteni().stream()
                .filter(friend -> !pathVisited.contains(friend.getId()))
                .mapToInt(friend -> 1 + findLongestPath(friend, pathVisited))
                .max()
                .orElse(0);
        pathVisited.remove(user.getId());
        return longestPath;
    }

    private Tuple<String, String> userName(Utilizator user) {
        return new Tuple<>(user.getNume(), user.getPrenume());
    }
}
