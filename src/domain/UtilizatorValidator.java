package domain;

import exceptions.ValidationException;

/** Validates user IDs and names. */
public class UtilizatorValidator implements Validator<Utilizator> {
    /**
     * Checks that a user has a positive ID and non-empty names starting with uppercase letters.
     *
     * @param entity the user to validate
     * @throws ValidationException if one or more user fields are invalid
     */
    @Override
    public void validate(Utilizator entity) {
        String errors = "";

        if (entity.getId() == null || entity.getId() <= 0) {
            errors += "Id-ul trebuie sa fie strict pozitiv!\n";
        }
        if (entity.getPrenume() == null || entity.getPrenume().trim().isEmpty() || !Character.isUpperCase(entity.getPrenume().charAt(0))) {
            errors += "Prenumele nu poate fi vid si trebuie sa inceapa cu litera mare!\n";
        }
        if (entity.getNume() == null || entity.getNume().trim().isEmpty() || !Character.isUpperCase(entity.getNume().charAt(0))) {
            errors += "Numele nu poate fi vid si trebuie sa inceapa cu litera mare!\n";
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}