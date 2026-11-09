package ui;

import domain.Utilizator;
import domain.Prietenie;
import domain.Tuple;
import exceptions.*;
import service.PrietenieService;
import service.UtilizatorService;

import java.util.Scanner;

/** Provides the interactive console interface for social-network operations. */
public class UI {
    private final UtilizatorService utilizatorService;
    private final PrietenieService prietenieService;
    private final Scanner scanner;

    /**
     * Creates a console UI for the service.
     *
     * @param utilizatorService the service used to perform user operations
     * @param prietenieService the service used to perform friendship operations
     * @throws IllegalArgumentException if either service is {@code null}
     */
    public UI(UtilizatorService utilizatorService, PrietenieService prietenieService) {
        if (utilizatorService == null || prietenieService == null) {
            throw new IllegalArgumentException("The services cannot be null!");
        }
        this.utilizatorService = utilizatorService;
        this.prietenieService = prietenieService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays the command menu and handles commands until the user exits.
     * Operation errors are printed to the console and the menu continues.
     *
     * @throws java.util.NoSuchElementException if console input ends before the user exits
     */
    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Social Network ---");
            System.out.println("1. Add user");
            System.out.println("2. Delete user");
            System.out.println("3. Add friendship");
            System.out.println("4. Delete friendship");
            System.out.println("5. Show number of communities");
            System.out.println("6. Show the most sociable community");
            System.out.println("7. Show all users");
            System.out.println("8. Show all friendships");
            System.out.println("0. Exit");
            System.out.print("Choose an option: ");

            String optiune = scanner.nextLine();

            try {
                switch (optiune) {
                    case "1":
                        addUtilizator();
                        break;
                    case "2":
                        delUtilizator();
                        break;
                    case "3":
                        addPrietenie();
                        break;
                    case "4":
                        delPrietenie();
                        break;
                    case "5":
                        showNrComunitati();
                        break;
                    case "6":
                        showMostSociabilaComunitate();
                        break;
                    case "7":
                        showUtilizatori();
                        break;
                    case "8":
                        showPrietenii();
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option!");
                }
            } catch (ValidationException | RepositoryException | ServiceException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                System.out.println("The operation is not implemented for the configured repository: " + e.getMessage());
            }
        }
    }

    /**
     * Reads and adds a user using values entered in the console.
     *
     * @throws ServiceException if a user with that name and surname already exists
     * @throws ValidationException if the user fields fail validation
     * @throws RepositoryException if persistence fails
     */
    private void addUtilizator() {
        System.out.print("Last name: ");
        String nume = scanner.nextLine();

        System.out.print("First name: ");
        String prenume = scanner.nextLine();
        Utilizator u = new Utilizator(nume, prenume);
        utilizatorService.addUtilizator(u);
        System.out.println("User added successfully!");
    }

    /**
     * Reads a user's name and surname and removes that user from the network.
     *
     * @throws IllegalArgumentException if the user key is {@code null}
     * @throws RepositoryException if persistence fails
     */
    private void delUtilizator() {
        Tuple<String, String> id = readUserKey("user to delete");
        Utilizator sters = utilizatorService.delUtilizator(id);
        if (sters != null) {
            System.out.println("The user was deleted.");
        } else {
            System.out.println("The user was not found.");
        }
    }

    /**
     * Reads two user name-and-surname pairs and creates a friendship between them.
     *
     * @throws ValidationException if the friendship cannot be created
     * @throws ServiceException if either user is missing
     * @throws RepositoryException if persistence fails
     */
    private void addPrietenie() {
        Tuple<String, String> id1 = readUserKey("first user");
        Tuple<String, String> id2 = readUserKey("second user");

        prietenieService.addPrietenie(id1, id2);
        System.out.println("Friendship added successfully!");
    }

    /**
     * Reads two user name-and-surname pairs and removes the friendship between them, if present.
     */
    private void delPrietenie() {
        Tuple<String, String> id1 = readUserKey("first user");
        Tuple<String, String> id2 = readUserKey("second user");

        if (prietenieService.delPrietenie(id1, id2) == null) {
            System.out.println("The friendship was not found.");
        } else {
            System.out.println("Friendship removed.");
        }
    }

    /** Prints the number of connected communities. */
    private void showNrComunitati() {
        int nr = prietenieService.getNrComunitati();
        System.out.println("Number of communities (connected components): " + nr);
    }

    /** Prints the users in the most sociable community. */
    private void showMostSociabilaComunitate() {
        System.out.println("The most sociable community consists of users:");
        prietenieService.getMostSociabilaComunitate().stream()
                .map(user -> user.getFirst() + " " + user.getSecond())
                .forEach(System.out::println);
    }

    /** Prints every user in the network. */
    private void showUtilizatori() {
        System.out.println("Users in the network:");
        utilizatorService.getAllUtilizatori().forEach(System.out::println);
    }

    /** Prints every friendship in the network. */
    private void showPrietenii() {
        final boolean[] existaPrietenii = {false};
        System.out.println("Friendships in the network:");
        prietenieService.getAllPrietenie().forEach(prietenie -> {
            Tuple<Tuple<String, String>, Tuple<String, String>> id = prietenie.getId();
            System.out.println(id.getFirst().getFirst() + " " + id.getFirst().getSecond() + " <-> " +
                    id.getSecond().getFirst() + " " + id.getSecond().getSecond() + ";" + prietenie.getData());
            existaPrietenii[0] = true;
        });
        if (!existaPrietenii[0]) {
            System.out.println("There are no friendships.");
        }
    }

    private Tuple<String, String> readUserKey(String label) {
        System.out.println("Enter the data for " + label + ":");
        System.out.print("Last name: ");
        String nume = scanner.nextLine();
        System.out.print("First name: ");
        String prenume = scanner.nextLine();
        return new Tuple<>(nume, prenume);
    }
}