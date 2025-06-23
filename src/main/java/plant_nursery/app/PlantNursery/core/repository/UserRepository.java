package plant_nursery.app.PlantNursery.core.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import plant_nursery.app.PlantNursery.core.repository.interfaces.IUserRepository;
import protobuf.*;

@Repository
public class UserRepository implements IUserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<User> USER_ROW_MAPPER = (resultSet, rowNum) -> {
        return User.newBuilder()
                .setId(resultSet.getLong("id"))
                .setUsername(resultSet.getString("name"))
                .setPassword(resultSet.getString("password"))
                .setRole(resultSet.getString("role"))
                .build();
    };

    public User findByUsername(String username) {
        String sql = "SELECT id, role, name, password FROM \"User\" WHERE name = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, username);
    }

    @Override
    public User CreateUser(CreateUserRequest createUserRequest) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO \"User\" (role, name, password) VALUES (?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, createUserRequest.getRole());
            ps.setString(2, createUserRequest.getUsername());
            ps.setString(3, createUserRequest.getPassword());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        GetUserRequest getUserRequest = GetUserRequest.newBuilder()
                .setId(id)
                .build();

        return GetUserById(getUserRequest);
    }

    @Override
    public User GetUserById(GetUserRequest getUserRequest) {
        try {
            String sql = "SELECT id, role, name, password FROM \"User\" WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, getUserRequest.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new RepositoryArgumentException("User with id " + getUserRequest.getId() + " not found");
        }
    }

    @Override
    public Users GetAllUsers(Empty empty) {
        String sql = "SELECT id, name, password, role FROM \"User\"";
        List<User> UsersList = jdbcTemplate.query(sql, USER_ROW_MAPPER);

        return Users.newBuilder()
                .addAllUsers(UsersList)
                .build();
    }

    @Override
    public User UpdateUser(User user) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE \"User\" SET role = ?, name = ?, password = ? WHERE id = ?",
                user.getRole(),
                user.getUsername(),
                user.getPassword(),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new RepositoryArgumentException("User with id " + user.getId() + " not found");
        }

        GetUserRequest getUserRequest = GetUserRequest.newBuilder()
                .setId(user.getId())
                .build();

        return GetUserById(getUserRequest);
    }

    @Override
    public void DeleteUser(DeleteUserRequest deleteUserRequest) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM \"User\" WHERE id = ?",
                deleteUserRequest.getId()
        );

        if (deletedRows == 0) {
            throw new RepositoryArgumentException("User with id " + deleteUserRequest.getId() + " not found");
        }
    }
}