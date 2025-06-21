package plant_nursery.app.PlantNursery.core.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.repository.UserRepository;
import protobuf.*;

@GRpcService
public class UserService extends UserServiceGrpc.UserServiceImplBase {
    @Autowired
    private UserRepository userRepository;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        User newUser = userRepository.CreateUser(request);
        responseObserver.onNext(newUser);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserById(GetUserRequest request, StreamObserver<User> responseObserver) {
        User user = userRepository.GetUserById(request);
        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }

    @Override
    public void updateUser(User request, StreamObserver<User> responseObserver) {
        User updatedUser = userRepository.UpdateUser(request);
        responseObserver.onNext(updatedUser);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<Empty> responseObserver) {
        userRepository.DeleteUser(request);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
}
