package domain;

import exceptions.ValidationException;

/**
 * Defines validation for objects of a specified type.
 *
 * @param <T> the type of object to validate
 */
public interface Validator <T>{
    /**
     * Checks whether an object satisfies its validation rules.
     *
     * @param entity the object to validate
     * @throws ValidationException if the object is invalid
     */
    void validate(T entity);
}
