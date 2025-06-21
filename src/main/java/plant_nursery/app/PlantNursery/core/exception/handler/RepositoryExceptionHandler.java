package plant_nursery.app.PlantNursery.core.exception.handler;

import io.grpc.Status;
import org.lognet.springboot.grpc.recovery.GRpcExceptionHandler;
import org.lognet.springboot.grpc.recovery.GRpcExceptionScope;
import org.lognet.springboot.grpc.recovery.GRpcServiceAdvice;

@GRpcServiceAdvice
public class RepositoryExceptionHandler {
    @GRpcExceptionHandler
    public Status repositoryHandle(RuntimeException exception, GRpcExceptionScope scope) {
        return scope
                .getHintAs(String.class)
                .map(Status.INVALID_ARGUMENT::withDescription)
                .orElse(Status.INVALID_ARGUMENT);
    }
}
