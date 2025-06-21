package plant_nursery.app.PlantNursery.core.exception;

import java.io.IOException;

public class RepositoryArgumentException extends RuntimeException {
    public RepositoryArgumentException() {}

    public RepositoryArgumentException(String message) {
        super(message);
    }
}
