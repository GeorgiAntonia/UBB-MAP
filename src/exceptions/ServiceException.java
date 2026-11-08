package exceptions;

/** Exception for business logic and service-level errors. */
public class ServiceException extends RuntimeException {
    /**
     * Creates a service exception.
     *
     * @param message the business-operation failure reason
     */
    public ServiceException(String message) {
        super(message);
    }
}