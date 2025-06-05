package plant_nursery.app.PlantNursery.core.auth;

import io.grpc.*;
import io.jsonwebtoken.*;

public class AuthInterceptor implements ServerInterceptor {

    public static final Context.Key<Object> USER_IDENTITY = Context.key("role");
    public static final Context.Key<Object> USER_ROLE = Context.key("user_role");

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
            status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
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
                            .withValue(Constant.CLIENT_ID_CONTEXT_KEY, claims.getBody().getSubject())
                            .withValue(USER_ROLE, claims.getBody().get("role"));
                return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
            }
        }


//        if (value == null) {
//            status = Status.UNAUTHENTICATED.withDescription("Authorization token is missing");
//        } else if (!value.startsWith(Constant.BEARER_TYPE)) {
//            status = Status.UNAUTHENTICATED.withDescription("Unknown authorization type");
//        } else {
//            Jws<Claims> claims = null;
//            // remove authorization type prefix
//            String token = value.substring(Constant.BEARER_TYPE.length()).trim();
//            try {
//                // verify token signature and parse claims
//                claims = parser.parseClaimsJws(token);
//            } catch (JwtException e) {
//                status = Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e);
//            }
//            if (claims != null) {
//                // set client id into current context
//                Context ctx = Context.current()
//                        .withValue(Constant.CLIENT_ID_CONTEXT_KEY, claims.getBody().getSubject())
//                        .withValue(USER_IDENTITY, claims.getBody().getSubject())
//                        .withValue(USER_ROLE, claims.getBody().getSubject());
//                return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
//            }
//        }

        serverCall.close(status, new Metadata());
        return new ServerCall.Listener<ReqT>() {
            // noop
        };
    }

}