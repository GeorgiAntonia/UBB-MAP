package domain;

import exceptions.ValidationException;

/** Validates user names. */
public class UtilizatorValidator implements Validator<Utilizator> {
    /**
     * Checks that both user names are non-empty and start with uppercase letters.
     *
     * @param entity the user to validate
     * @throws ValidationException if one or more user fields are invalid
     */
    @Override
    public void validate(Utilizator entity) {
        String errors = "";

        if (entity.getPrenume() == null || entity.getPrenume().trim().isEmpty() || !Character.isUpperCase(entity.getPrenume().charAt(0))) {
            errors += "The first name cannot be empty and must start with an uppercase letter!\n";
        }
        if (entity.getNume() == null || entity.getNume().trim().isEmpty() || !Character.isUpperCase(entity.getNume().charAt(0))) {
            errors += "The last name cannot be empty and must start with an uppercase letter!\n";
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}