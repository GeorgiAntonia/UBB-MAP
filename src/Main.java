import domain.Prietenie;
import domain.Tuple;
import domain.Utilizator;
import domain.UtilizatorValidator;
import domain.Validator;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import repository.Repository;
import repository.RepositoryFactory;
import service.PrietenieService;
import service.UtilizatorService;
import ui.UI;

public class Main {
    public static void main(String[] args) {
        try {
            Validator<Utilizator> utilizatorValidator = new UtilizatorValidator()
                    .andThen(utilizator -> {
                        if (utilizator.getNume() != null && utilizator.getNume().equals(utilizator.getPrenume())) {
                            throw new ValidationException("The last name and first name cannot be identical!");
                        }
                    });

            Validator<Prietenie> prietenieValidator = prietenie -> {
                if (prietenie.getId() == null || prietenie.getId().getFirst() == null || prietenie.getId().getSecond() == null) {
                    throw new ValidationException("The friendship users cannot be null!");
                }
                if (prietenie.getId().getFirst().equals(prietenie.getId().getSecond())) {
                    throw new ValidationException("A user cannot be friends with themselves!");
                }
                if (prietenie.getData() == null) {
                    throw new ValidationException("The friendship date cannot be null!");
                }
            };

            RepositoryFactory factory = new RepositoryFactory();
            Repository<Tuple<String, String>, Utilizator> utilizatorRepository = factory.createUtilizatorRepository(utilizatorValidator);
            Repository<Tuple<Tuple<String, String>, Tuple<String, String>>, Prietenie> prietenieRepository = factory.createPrietenieRepository(prietenieValidator);
            factory.initializeFriendships(utilizatorRepository, prietenieRepository);

            PrietenieService prietenieService = new PrietenieService(utilizatorRepository, prietenieRepository);
            UtilizatorService utilizatorService = new UtilizatorService(utilizatorRepository, prietenieService);
            new UI(utilizatorService, prietenieService).start();
        } catch (RepositoryException e) {
            System.err.println("Error initializing repositories: " + e.getMessage());
        }
    }
}