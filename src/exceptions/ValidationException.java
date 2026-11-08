package exceptions;

/**
 * Unchecked exception thrown when a domain object fails validation.
 */
public class ValidationException extends RuntimeException {
    /**
     * Creates a validation exception with a message.
     *
     * @param message the validation failure reason
     */
    public ValidationException(String message) {
        super(message);
    }
}
