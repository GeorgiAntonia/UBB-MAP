package exceptions;

/**
 * Unchecked exception for persistence and repository failures.
 */
public class RepositoryException extends RuntimeException {
    /**
     * Creates a repository exception with a message.
     *
     * @param message the reason the repository operation failed
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Creates a repository exception and preserves its underlying cause.
     *
     * @param message the reason the repository operation failed
     * @param cause the underlying persistence failure
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
