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
            Validator<Utilizator> utilizatorValidator = new UtilizatorValidator();

            Validator<Prietenie> prietenieValidator = new Validator<Prietenie>() {
                @Override
                public void validate(Prietenie prietenie) {
                    if (prietenie.getId() == null || prietenie.getId().getFirst() == null || prietenie.getId().getSecond() == null) {
                        throw new ValidationException("ID-urile prieteniei nu pot fi null!");
                    }
                    if (prietenie.getId().getFirst().equals(prietenie.getId().getSecond())) {
                        throw new ValidationException("Un utilizator nu se poate imprieteni cu el insusi!");
                    }
                    if (prietenie.getData() == null) {
                        throw new ValidationException("Data prieteniei nu poate fi null!");
                    }
                }
            };

            RepositoryFactory factory = new RepositoryFactory();
            Repository<Long, Utilizator> utilizatorRepository = factory.createUtilizatorRepository(utilizatorValidator);
            Repository<Tuple<Long, Long>, Prietenie> prietenieRepository = factory.createPrietenieRepository(prietenieValidator);
            factory.initializeFriendships(utilizatorRepository, prietenieRepository);

            PrietenieService prietenieService = new PrietenieService(utilizatorRepository, prietenieRepository);
            UtilizatorService utilizatorService = new UtilizatorService(utilizatorRepository, prietenieService);
            new UI(utilizatorService, prietenieService).start();
        } catch (RepositoryException e) {
            System.err.println("Eroare la initializarea repository-urilor: " + e.getMessage());
        }
    }
}