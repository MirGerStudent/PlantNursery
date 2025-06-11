package plant_nursery.app.PlantNursery.core.auth;

import build.buf.protovalidate.ValidationResult;
import build.buf.protovalidate.Validator;
import build.buf.protovalidate.ValidatorFactory;
import build.buf.protovalidate.exceptions.ValidationException;
import io.grpc.*;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@GRpcGlobalInterceptor
@Order(2)
public class ValidationInterceptor implements ServerInterceptor {

    private final Validator validator = ValidatorFactory.newBuilder().build();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next
    ) {
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void sendMessage(RespT message) {
                // Валидация ответа (опционально)
                super.sendMessage(message);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(wrappedCall, headers)) {
            @Override
            public void onMessage(ReqT message) {
                // Валидация запроса
                ValidationResult result = null;
                try {
                    result = validator.validate((com.google.protobuf.Message) message);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                if (!result.isSuccess()) {
                    call.close(Status.INVALID_ARGUMENT.withDescription(result.toString()), new Metadata());
                    return;
                }
                super.onMessage(message);
            }
        };
    }
}
