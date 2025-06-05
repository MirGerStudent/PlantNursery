package plant_nursery.app.PlantNursery.core.auth;

import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class Constant {
    public static final String JWT_SIGNING_KEY = "L8hHXsaQOUjk5rg7XPGv4eLl6anlCrkMz8CJ0i/8E/0=";
    public static final String BEARER_TYPE = "Bearer";

    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> CLIENT_ID_CONTEXT_KEY = Context.key("clientId");

    public static boolean adminCheck(String ROLE_KEY, StreamObserver<?> responseObserver) {
        // Check the user's role
//        String role = ROLE_KEY.get(Context.current());
//        String role = Context.key("role").get().toString();
        if (!"ADMIN".equals(ROLE_KEY)) {
            responseObserver.onError(Status.PERMISSION_DENIED
                    .withDescription(String.format("Only admins can delete events %s, %s, %s", ROLE_KEY, CLIENT_ID_CONTEXT_KEY, AUTHORIZATION_METADATA_KEY.name()))
                    .asRuntimeException());
            return false;
        }
        return true;
    };

    private Constant() {
        throw new AssertionError();
    }
}