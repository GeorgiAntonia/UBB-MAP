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
    private final Repository<Tuple<String, String>, Utilizator> utilizatorRepository;
    private final Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie> prietenieRepository;

    /**
     * Creates a friendship service backed by the supplied repositories.
     *
     * @param utilizatorRepository the repository used to find users
     * @param prietenieRepository the repository used to store friendships
     * @throws IllegalArgumentException if either repository is {@code null}
     */
    public PrietenieService(
            Repository<Tuple<String, String>, Utilizator> utilizatorRepository,
            Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie> prietenieRepository) {
        if (utilizatorRepository == null || prietenieRepository == null) {
            throw new IllegalArgumentException("The repositories cannot be null!");
        }
        this.utilizatorRepository = utilizatorRepository;
        this.prietenieRepository = prietenieRepository;
    }

    /**
     * Creates a friendship between two distinct users.
     *
     * @param id1 the name and surname of the first user
     * @param id2 the name and surname of the second user
     * @throws IllegalArgumentException if either user key is {@code null}
     * @throws ServiceException if either user is absent or the friendship already exists
     * @throws ValidationException if both user keys are equal or the friendship is invalid
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public void addPrietenie(Tuple<String, String> id1, Tuple<String, String> id2) {
        Tuple<Tuple<String, String>, Tuple<String, String>> friendshipId = normalizedId(id1, id2);
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

        Prietenie saved = prietenieRepository.save(new Prietenie(id1, id2));
        if (saved != null) {
            throw new ServiceException("The friendship already exists!");
        }

        first.addPrieten(second);
        second.addPrieten(first);
    }

    /**
     * Removes the friendship between two users if it exists.
     *
     * @param id1 the name and surname of the first user
     * @param id2 the name and surname of the second user
     * @return the removed friendship, or {@code null} if it did not exist
     * @throws IllegalArgumentException if either user key is {@code null}
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public Prietenie delPrietenie(Tuple<String, String> id1, Tuple<String, String> id2) {
        Tuple<Tuple<String, String>, Tuple<String, String>> friendshipId = normalizedId(id1, id2);
        Prietenie deleted = prietenieRepository.delete(friendshipId);

        if (deleted != null) {
            Utilizator first = utilizatorRepository.findOne(id1);
            Utilizator second = utilizatorRepository.findOne(id2);
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
     * @param id the name and surname identifying the user whose friendships should be removed
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public void delPrieteniiUtilizator(Tuple<String, String> id) {
        if (id == null) {
            throw new IllegalArgumentException("The user key cannot be null!");
        }
        Utilizator user = utilizatorRepository.findOne(id);
        if (user == null) {
            return;
        }

        List<Tuple<Tuple<String, String>, Tuple<String, String>>> friendshipIds =
                StreamSupport.stream(prietenieRepository.findAll().spliterator(), false)
                        .map(Prietenie::getId)
                        .filter(friendshipId -> id.equals(friendshipId.getFirst()) || id.equals(friendshipId.getSecond()))
                        .collect(Collectors.toList());

        friendshipIds.forEach(friendshipId ->
                delPrietenie(friendshipId.getFirst(), friendshipId.getSecond()));
    }

    /**
     * Returns all friendships in the network.
     *
     * @return all friendships
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public Iterable<Prietenie> getAllPrietenie() {
        return prietenieRepository.findAll();
    }

    /**
     * Counts the connected components in the friendship graph.
     *
     * @return the number of communities; isolated users each count as one community
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public int getNrComunitati() {
        Set<Tuple<String, String>> visited = new HashSet<>();
        return (int) StreamSupport.stream(utilizatorRepository.findAll().spliterator(), false)
                .filter(user -> !visited.contains(userKey(user)))
                .peek(user -> visitCommunity(user, visited))
                .count();
    }

    /**
     * Finds the community containing the longest simple friendship path.
     *
     * @return name-and-surname pairs of users in that community, or an empty list if the network is empty
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public List<Tuple<String, String>> getMostSociabilaComunitate() {
        Set<Tuple<String, String>> visited = new HashSet<>();
        List<Tuple<String, String>> longestCommunity = new ArrayList<>();
        List<Integer> longestPath = new ArrayList<>();

        StreamSupport.stream(utilizatorRepository.findAll().spliterator(), false)
                .filter(user -> !visited.contains(userKey(user)))
                .forEach(user -> {
                    List<Utilizator> component = new ArrayList<>();
                    collectCommunity(user, visited, component);
                    int componentLongestPath = findLongestPathInComponent(component);
                    if (longestPath.isEmpty() || componentLongestPath > longestPath.get(0)) {
                        longestPath.clear();
                        longestPath.add(componentLongestPath);
                        longestCommunity.clear();
                        longestCommunity.addAll(component.stream().map(this::userKey).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
                    }
                });
        return longestCommunity;
    }

    /**
     * Creates the canonical friendship key in name-and-surname order.
     *
     * @param id1 the name and surname of one user
     * @param id2 the name and surname of the other user
     * @return the user keys in ascending order
     * @throws IllegalArgumentException if either key is {@code null}
     */
    private Tuple<Tuple<String, String>, Tuple<String, String>> normalizedId(
            Tuple<String, String> id1, Tuple<String, String> id2) {
        if (id1 == null || id2 == null || id1.getFirst() == null || id1.getSecond() == null ||
                id2.getFirst() == null || id2.getSecond() == null) {
            throw new IllegalArgumentException("The user identification data cannot be null!");
        }
        int compareNume = id1.getFirst().compareTo(id2.getFirst());
        int compare = compareNume != 0 ? compareNume : id1.getSecond().compareTo(id2.getSecond());
        return compare <= 0 ? new Tuple<>(id1, id2) : new Tuple<>(id2, id1);
    }

    /**
     * Marks every user reachable from the given user as visited.
     *
     * @param user the starting user
     * @param visited the user keys already visited during traversal
     */
    private void visitCommunity(Utilizator user, Set<Tuple<String, String>> visited) {
        visited.add(userKey(user));
        user.getPrieteni().stream()
                .filter(friend -> !visited.contains(userKey(friend)))
                .forEach(friend -> visitCommunity(friend, visited));
    }

    /**
     * Collects all users reachable from the given user.
     *
     * @param user the starting user
     * @param visited the user keys already visited during traversal
     * @param component the list to populate with users in this community
     */
    private void collectCommunity(Utilizator user, Set<Tuple<String, String>> visited, List<Utilizator> component) {
        visited.add(userKey(user));
        component.add(user);
        user.getPrieteni().stream()
                .filter(friend -> !visited.contains(userKey(friend)))
                .forEach(friend -> collectCommunity(friend, visited, component));
    }

    /**
     * Finds the longest simple path among all users in a connected component.
     *
     * @param component the users belonging to one community
     * @return the longest path length, measured in friendship edges
     */
    private int findLongestPathInComponent(List<Utilizator> component) {
        return component.stream()
                .mapToInt(start -> findLongestPath(start, new HashSet<>()))
                .max()
                .orElse(0);
    }

    /**
     * Finds the longest simple path starting at a user.
     *
     * @param user the current user
     * @param pathVisited the user keys on the current path
     * @return the longest path length from {@code user}, measured in edges
     */
    private int findLongestPath(Utilizator user, Set<Tuple<String, String>> pathVisited) {
        pathVisited.add(userKey(user));
        int longestPath = user.getPrieteni().stream()
                .filter(friend -> !pathVisited.contains(userKey(friend)))
                .mapToInt(friend -> 1 + findLongestPath(friend, pathVisited))
                .max()
                .orElse(0);
        pathVisited.remove(userKey(user));
        return longestPath;
    }

    private Tuple<String, String> userKey(Utilizator user) {
        return new Tuple<>(user.getNume(), user.getPrenume());
    }
}
