package plant_nursery.app.PlantNursery.core.auth;

import io.grpc.*;
import io.jsonwebtoken.*;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@GRpcGlobalInterceptor
@Order(1)
public class AuthInterceptor implements ServerInterceptor {
    private final JwtParser parser = Jwts.parser().setSigningKey(Constant.JWT_SIGNING_KEY);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall,
            Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler
    ) {
        String value = metadata.get(Constant.AUTHORIZATION_METADATA_KEY);

        Status status = Status.OK;
        if (value == null) {
            String methodName = serverCall.getMethodDescriptor().getFullMethodName();
            if (methodName.equals("AuthService/Login")) {
                return serverCallHandler.startCall(serverCall, metadata);
            } else {
                status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
            }
        } else if (!value.startsWith(Constant.BEARER_TYPE)) {
            status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type");
        } else {
            Jws<Claims> claims = null;
            // remove authorization type prefix
            String token = value.substring(Constant.BEARER_TYPE.length()).trim();
            try {
                // verify token signature and parse claims
                claims = parser.parseClaimsJws(token);
            } catch (JwtException e) {
                status = Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e);
            }
            if (claims != null) {
                // set client id into current context
                Context ctx = Context.current()
                            .withValue(Constant.CLIENT_ID_CONTEXT_KEY, claims.getBody().getSubject());
                return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
            }
        }

        serverCall.close(status, new Metadata());
        return new ServerCall.Listener<ReqT>() {
            // noop
        };
    }
}