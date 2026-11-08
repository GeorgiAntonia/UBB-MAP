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
            throw new IllegalArgumentException("Repository-urile nu pot fi null!");
        }
        this.utilizatorRepository = utilizatorRepository;
        this.prietenieRepository = prietenieRepository;
    }

    /**
     * Creates a friendship between two distinct users.
     *
     * @param id1 the ID of the first user
     * @param id2 the ID of the second user
     * @throws IllegalArgumentException if either ID is {@code null}
     * @throws ServiceException if either user is absent or the friendship already exists
     * @throws ValidationException if the IDs are equal or the friendship is invalid
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public void addPrietenie(Long id1, Long id2) {
        Tuple<Long, Long> friendshipId = normalizedId(id1, id2);
        Utilizator first = utilizatorRepository.findOne(id1);
        Utilizator second = utilizatorRepository.findOne(id2);

        if (first == null || second == null) {
            throw new ServiceException("Utilizatorii nu exista!");
        }
        if (id1.equals(id2)) {
            throw new ValidationException("Un utilizator nu se poate imprieteni cu el insusi!");
        }
        if (prietenieRepository.findOne(friendshipId) != null) {
            throw new ServiceException("Prietenia exista deja!");
        }

        Prietenie saved = prietenieRepository.save(new Prietenie(id1, id2));
        if (saved != null) {
            throw new ServiceException("Prietenia exista deja!");
        }

        first.addPrieten(second);
        second.addPrieten(first);
    }

    /**
     * Removes the friendship between two users if it exists.
     *
     * @param id1 the ID of the first user
     * @param id2 the ID of the second user
     * @return the removed friendship, or {@code null} if it did not exist
     * @throws IllegalArgumentException if either ID is {@code null}
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public Prietenie delPrietenie(Long id1, Long id2) {
        Tuple<Long, Long> friendshipId = normalizedId(id1, id2);
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
     * @param id the ID of the user whose friendships should be removed
     * @throws IllegalArgumentException if {@code id} is {@code null}
     * @throws RepositoryException if persistence fails
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public void delPrieteniiUtilizator(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id-ul nu poate fi null!");
        }
        Utilizator user = utilizatorRepository.findOne(id);
        if (user == null) {
            return;
        }

        List<Tuple<Long, Long>> friendshipsToDelete = new ArrayList<>();
        for (Prietenie friendship : prietenieRepository.findAll()) {
            Tuple<Long, Long> friendshipId = friendship.getId();
            if (id.equals(friendshipId.getFirst()) || id.equals(friendshipId.getSecond())) {
                friendshipsToDelete.add(friendshipId);
            }
        }
        for (Tuple<Long, Long> friendshipId : friendshipsToDelete) {
            delPrietenie(friendshipId.getFirst(), friendshipId.getSecond());
        }
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
        Set<Long> visited = new HashSet<>();
        int count = 0;
        for (Utilizator user : utilizatorRepository.findAll()) {
            if (!visited.contains(user.getId())) {
                count++;
                visitCommunity(user, visited);
            }
        }
        return count;
    }

    /**
     * Finds the community containing the longest simple friendship path.
     *
     * @return IDs of users in that community, or an empty list if the network is empty
     * @throws UnsupportedOperationException if the configured repository is not implemented
     */
    public List<Long> getMostSociabilaComunitate() {
        Set<Long> visited = new HashSet<>();
        List<Long> longestCommunity = new ArrayList<>();
        int longestPath = -1;

        for (Utilizator user : utilizatorRepository.findAll()) {
            if (!visited.contains(user.getId())) {
                List<Utilizator> component = new ArrayList<>();
                collectCommunity(user, visited, component);
                int componentLongestPath = findLongestPathInComponent(component);
                if (componentLongestPath > longestPath) {
                    longestPath = componentLongestPath;
                    longestCommunity.clear();
                    for (Utilizator member : component) {
                        longestCommunity.add(member.getId());
                    }
                }
            }
        }
        return longestCommunity;
    }

    /**
     * Creates the canonical friendship ID with the smaller user ID first.
     *
     * @param id1 the ID of one user
     * @param id2 the ID of the other user
     * @return the IDs in ascending order
     * @throws IllegalArgumentException if either ID is {@code null}
     */
    private Tuple<Long, Long> normalizedId(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            throw new IllegalArgumentException("ID-urile utilizatorilor nu pot fi null!");
        }
        return new Tuple<>(Math.min(id1, id2), Math.max(id1, id2));
    }

    /**
     * Marks every user reachable from the given user as visited.
     *
     * @param user the starting user
     * @param visited the IDs already visited during traversal
     */
    private void visitCommunity(Utilizator user, Set<Long> visited) {
        visited.add(user.getId());
        for (Utilizator friend : user.getPrieteni()) {
            if (!visited.contains(friend.getId())) {
                visitCommunity(friend, visited);
            }
        }
    }

    /**
     * Collects all users reachable from the given user.
     *
     * @param user the starting user
     * @param visited the IDs already visited during traversal
     * @param component the list to populate with users in this community
     */
    private void collectCommunity(Utilizator user, Set<Long> visited, List<Utilizator> component) {
        visited.add(user.getId());
        component.add(user);
        for (Utilizator friend : user.getPrieteni()) {
            if (!visited.contains(friend.getId())) {
                collectCommunity(friend, visited, component);
            }
        }
    }

    /**
     * Finds the longest simple path among all users in a connected component.
     *
     * @param component the users belonging to one community
     * @return the longest path length, measured in friendship edges
     */
    private int findLongestPathInComponent(List<Utilizator> component) {
        int longestPath = 0;
        for (Utilizator start : component) {
            longestPath = Math.max(longestPath, findLongestPath(start, new HashSet<Long>()));
        }
        return longestPath;
    }

    /**
     * Finds the longest simple path starting at a user.
     *
     * @param user the current user
     * @param pathVisited the IDs on the current path
     * @return the longest path length from {@code user}, measured in edges
     */
    private int findLongestPath(Utilizator user, Set<Long> pathVisited) {
        pathVisited.add(user.getId());
        int longestPath = 0;
        for (Utilizator friend : user.getPrieteni()) {
            if (!pathVisited.contains(friend.getId())) {
                longestPath = Math.max(longestPath, 1 + findLongestPath(friend, pathVisited));
            }
        }
        pathVisited.remove(user.getId());
        return longestPath;
    }
}
