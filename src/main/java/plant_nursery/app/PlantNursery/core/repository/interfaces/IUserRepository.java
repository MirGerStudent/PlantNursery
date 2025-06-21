package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.*;

public interface IUserRepository {
    User findByUsername(String username);
    User CreateUser(CreateUserRequest CcreateUserRequest);
    User GetUserById(GetUserRequest getUserRequest);
    User UpdateUser(User user);
    void DeleteUser(DeleteUserRequest deleteUserRequest);
}
