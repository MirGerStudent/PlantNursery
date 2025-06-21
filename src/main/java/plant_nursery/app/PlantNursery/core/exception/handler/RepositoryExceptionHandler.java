package plant_nursery.app.PlantNursery.core.exception.handler;

import io.grpc.Status;
import org.lognet.springboot.grpc.recovery.GRpcExceptionHandler;
import org.lognet.springboot.grpc.recovery.GRpcExceptionScope;
import org.lognet.springboot.grpc.recovery.GRpcServiceAdvice;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import plant_nursery.app.PlantNursery.core.exception.RepositoryDataException;

@GRpcServiceAdvice
public class RepositoryExceptionHandler {
    @GRpcExceptionHandler
    public Status repositoryArgumentHandle(RepositoryArgumentException exception, GRpcExceptionScope scope) {
        return scope
                .getHintAs(String.class)
                .map(Status.INVALID_ARGUMENT::withDescription)
                .orElse(Status.INVALID_ARGUMENT);
    }

    public Status repositoryDataException(RepositoryDataException exception, GRpcExceptionScope scope) {
        return scope
                .getHintAs(String.class)
                .map(Status.UNAVAILABLE::withDescription)
                .orElse(Status.UNAVAILABLE);
    }
}
