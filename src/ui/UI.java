package ui;

import domain.Utilizator;
import domain.Prietenie;
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
            throw new IllegalArgumentException("Serviciile nu pot fi null!");
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
            System.out.println("\n--- Retea de Socializare ---");
            System.out.println("1. Adauga utilizator");
            System.out.println("2. Sterge utilizator");
            System.out.println("3. Adauga prietenie");
            System.out.println("4. Sterge prietenie");
            System.out.println("5. Afiseaza numarul de comunitati");
            System.out.println("6. Afiseaza cea mai sociabila comunitate");
            System.out.println("7. Afiseaza toti utilizatorii");
            System.out.println("8. Afiseaza toate prieteniile");
            System.out.println("0. Iesire");
            System.out.print("Alegeti o optiune: ");

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
                        System.out.println("Optiune invalida!");
                }
            } catch (ValidationException | RepositoryException | ServiceException e) {
                System.out.println("Eroare: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("Date introduse invalide: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                System.out.println("Operatia nu este implementata pentru repository-ul configurat: " + e.getMessage());
            }
        }
    }

    /**
     * Reads and adds a user using values entered in the console.
     *
     * @throws NumberFormatException if the entered ID is not a valid long
     * @throws ServiceException if a user with that ID already exists
     * @throws ValidationException if the user fields fail validation
     * @throws RepositoryException if persistence fails
     */
    private void addUtilizator() {
        System.out.print("Introduceti ID: ");
        Long id = Long.parseLong(scanner.nextLine());

        System.out.print("Nume: ");
        String nume = scanner.nextLine();

        System.out.print("Prenume: ");
        String prenume = scanner.nextLine();

        Utilizator u = new Utilizator(nume, prenume);
        u.setId(id);
        utilizatorService.addUtilizator(u);
        System.out.println("Utilizator adaugat cu succes!");
    }

    /**
     * Reads a user ID and removes that user from the network.
     *
     * @throws NumberFormatException if the entered ID is not a valid long
     * @throws IllegalArgumentException if the ID is {@code null}
     * @throws RepositoryException if persistence fails
     */
    private void delUtilizator() {
        System.out.print("ID-ul utilizatorului de sters: ");
        Long id = Long.parseLong(scanner.nextLine());
        Utilizator sters = utilizatorService.delUtilizator(id);
        if (sters != null) {
            System.out.println("Utilizatorul a fost sters.");
        } else {
            System.out.println("Utilizatorul nu a fost gasit.");
        }
    }

    /**
     * Reads two user IDs and creates a friendship between them.
     *
     * @throws NumberFormatException if either entered ID is not a valid long
     * @throws ValidationException if the friendship cannot be created
     * @throws ServiceException if either user is missing
     * @throws RepositoryException if persistence fails
     */
    private void addPrietenie() {
        System.out.print("ID utilizator 1: ");
        Long id1 = Long.parseLong(scanner.nextLine());
        System.out.print("ID utilizator 2: ");
        Long id2 = Long.parseLong(scanner.nextLine());

        prietenieService.addPrietenie(id1, id2);
        System.out.println("Prietenie adaugata cu succes!");
    }

    /**
     * Reads two user IDs and removes the friendship between them, if present.
     *
     * @throws NumberFormatException if either entered ID is not a valid long
     */
    private void delPrietenie() {
        System.out.print("ID utilizator 1: ");
        Long id1 = Long.parseLong(scanner.nextLine());
        System.out.print("ID utilizator 2: ");
        Long id2 = Long.parseLong(scanner.nextLine());

        if (prietenieService.delPrietenie(id1, id2) == null) {
            System.out.println("Prietenia nu a fost gasita.");
        } else {
            System.out.println("Prietenie stearsa.");
        }
    }

    /** Prints the number of connected communities. */
    private void showNrComunitati() {
        int nr = prietenieService.getNrComunitati();
        System.out.println("Numarul de comunitati (componente conexe): " + nr);
    }

    /** Prints the IDs of the users in the most sociable community. */
    private void showMostSociabilaComunitate() {
        System.out.println("Cea mai sociabila comunitate este formata din utilizatorii cu ID-urile: " +
                prietenieService.getMostSociabilaComunitate());
    }

    /** Prints every user in the network. */
    private void showUtilizatori() {
        System.out.println("Utilizatorii din retea:");
        for (Utilizator utilizator : utilizatorService.getAllUtilizatori()) {
            System.out.println(utilizator);
        }
    }

    /** Prints every friendship in the network. */
    private void showPrietenii() {
        boolean existaPrietenii = false;
        System.out.println("Prieteniile din retea:");
        for (Prietenie prietenie : prietenieService.getAllPrietenie()) {
            System.out.println(prietenie.getId().getFirst() + ";" +
                    prietenie.getId().getSecond() + ";" + prietenie.getData());
            existaPrietenii = true;
        }
        if (!existaPrietenii) {
            System.out.println("Nu exista prietenii.");
        }
    }
}