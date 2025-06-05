package plant_nursery.app.PlantNursery.core.service;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.auth.JwtUtil;
import plant_nursery.app.PlantNursery.core.repository.UserRepository;
import protobuf.*;

@GRpcService
public class AuthService extends AuthServiceGrpc.AuthServiceImplBase {

    @Autowired
    private UserRepository userRepository;
    private static final Context.Key<String> ROLE_KEY = Context.key("role");

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {
            User user = userRepository.findByUsername(request.getUsername());
            if (user.getPassword().equals(request.getPassword())) { // Хеширование!
                String token = JwtUtil.generateToken(user.getUsername(), user.getRole());
                LoginResponse response = LoginResponse.newBuilder().setToken(token).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.UNAUTHENTICATED.withDescription("Invalid credentials").asRuntimeException());
            }
        } catch (Exception e) {
            responseObserver.onError(Status.UNAUTHENTICATED.withDescription("User not found").asRuntimeException());
        }
    }

    @Override
    public void workerAction(WorkerRequest request, StreamObserver<WorkerResponse> responseObserver) {
        String role = Context.key("role").get().toString();
//        String role = ROLE_KEY.get(Context.current());
        if (!"WORKER".equals(role) && !"ADMIN".equals(role)) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Access denied").asRuntimeException());
            return;
        }
        WorkerResponse response = WorkerResponse.newBuilder()
                .setResult("Worker processed: " + request.getData())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void adminAction(AdminRequest request, StreamObserver<AdminResponse> responseObserver) {
//        String role = Context.current().toString();
        String role = Context.key("role LOL").get().toString();

//        String role = ROLE_KEY.get(Context.current());
        if (!"ADMIN".equals(role)) {
            responseObserver.onError(Status.PERMISSION_DENIED.withDescription("Admin access required").asRuntimeException());
            return;
        }
        AdminResponse response = AdminResponse.newBuilder()
                .setResult("Admin processed: " + request.getData())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
