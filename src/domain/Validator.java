package domain;

import exceptions.ValidationException;

/**
 * Defines validation for objects of a specified type.
 *
 * @param <T> the type of object to validate
 */
@FunctionalInterface
public interface Validator<T> {
    /**
     * Checks whether an object satisfies its validation rules.
     *
     * @param entity the object to validate
     * @throws ValidationException if the object is invalid
     */
    void validate(T entity);

    /**
     * Composes this validator with another one so both rules are enforced.
     *
     * @param other the next validation rule to apply
     * @return the combined validator
     * @throws IllegalArgumentException if {@code other} is {@code null}
     */
    default Validator<T> andThen(Validator<? super T> other) {
        if (other == null) {
            throw new IllegalArgumentException("The validator cannot be null!");
        }
        return entity -> {
            validate(entity);
            other.validate(entity);
        };
    }
}
