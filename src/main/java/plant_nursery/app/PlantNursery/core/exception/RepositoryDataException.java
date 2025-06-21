package plant_nursery.app.PlantNursery.core.exception;

public class RepositoryDataException extends RuntimeException {
    public RepositoryDataException() {
    }

    public RepositoryDataException(String message) {
        super(message);
    }

    public RepositoryDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
