package plant_nursery.app.PlantNursery.core.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import protobuf.User;

@Repository
public class UserRepository {
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
        String sql = "SELECT (id, role, name, password) FROM \"User\" WHERE name = ?";
        return jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, username);
    }
}