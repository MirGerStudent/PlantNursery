package plant_nursery.app.PlantNursery.core.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import protobuf.CreateUserRequest;
import protobuf.DeleteUserRequest;
import protobuf.GetUserRequest;
import protobuf.User;
import protobuf.Users;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("PlantNurseryTest");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("grpc.enabled", () -> "false");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS \"User\" (" +
                "id BIGSERIAL PRIMARY KEY, role VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL)");
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    void testCreateUser() {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setUsername("testuser")
                .setPassword("password123")
                .setRole("WORKER")
                .build();

        User created = userRepository.CreateUser(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getUsername()).isEqualTo("testuser");
        assertThat(created.getRole()).isEqualTo("WORKER");
    }

    @Test
    void testGetUserById_Found() {
        CreateUserRequest createRequest = CreateUserRequest.newBuilder()
                .setUsername("testuser2")
                .setPassword("password456")
                .setRole("ADMIN")
                .build();
        User created = userRepository.CreateUser(createRequest);

        GetUserRequest getRequest = GetUserRequest.newBuilder()
                .setId(created.getId())
                .build();

        User fetched = userRepository.GetUserById(getRequest);

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getUsername()).isEqualTo("testuser2");
    }

    @Test
    void testGetUserById_NotFound() {
        GetUserRequest getRequest = GetUserRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> userRepository.GetUserById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testFindByUsername() {
        CreateUserRequest createRequest = CreateUserRequest.newBuilder()
                .setUsername("testuser3")
                .setPassword("password789")
                .setRole("WORKER")
                .build();
        userRepository.CreateUser(createRequest);

        User found = userRepository.findByUsername("testuser3");

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("testuser3");
    }

    @Test
    void testUpdateUser() {
        CreateUserRequest createRequest = CreateUserRequest.newBuilder()
                .setUsername("oldusername")
                .setPassword("oldpassword")
                .setRole("WORKER")
                .build();
        User created = userRepository.CreateUser(createRequest);

        User updatedUser = User.newBuilder()
                .setId(created.getId())
                .setUsername("newusername")
                .setPassword("newpassword")
                .setRole("ADMIN")
                .build();

        User updated = userRepository.UpdateUser(updatedUser);

        assertThat(updated.getUsername()).isEqualTo("newusername");
        assertThat(updated.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void testUpdateUser_NotFound() {
        User user = User.newBuilder()
                .setId(-1L)
                .setUsername("nonexistent")
                .setPassword("password")
                .setRole("WORKER")
                .build();

        assertThatThrownBy(() -> userRepository.UpdateUser(user))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testDeleteUser() {
        CreateUserRequest createRequest = CreateUserRequest.newBuilder()
                .setUsername("todelete")
                .setPassword("password")
                .setRole("WORKER")
                .build();
        User created = userRepository.CreateUser(createRequest);

        DeleteUserRequest deleteRequest = DeleteUserRequest.newBuilder()
                .setId(created.getId())
                .build();

        userRepository.DeleteUser(deleteRequest);

        GetUserRequest getRequest = GetUserRequest.newBuilder()
                .setId(created.getId())
                .build();

        assertThatThrownBy(() -> userRepository.GetUserById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class);
    }

    @Test
    void testDeleteUser_NotFound() {
        DeleteUserRequest deleteRequest = DeleteUserRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> userRepository.DeleteUser(deleteRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testGetAllUsers() {
        CreateUserRequest createRequest1 = CreateUserRequest.newBuilder()
                .setUsername("user1")
                .setPassword("pass1")
                .setRole("WORKER")
                .build();
        userRepository.CreateUser(createRequest1);

        CreateUserRequest createRequest2 = CreateUserRequest.newBuilder()
                .setUsername("user2")
                .setPassword("pass2")
                .setRole("ADMIN")
                .build();
        userRepository.CreateUser(createRequest2);

        Users allUsers = userRepository.GetAllUsers(protobuf.Empty.newBuilder().build());

        assertThat(allUsers.getUsersCount()).isGreaterThanOrEqualTo(2);
    }
}
