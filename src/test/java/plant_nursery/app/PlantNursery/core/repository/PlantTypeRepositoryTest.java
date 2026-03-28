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
import protobuf.AllPlantTypesResponse;
import protobuf.CreatePlantTypeRequest;
import protobuf.GetPlantTypeRequest;
import protobuf.PlantType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlantTypeRepositoryTest {
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
    PlantTypeRepository plantTypeRepository;

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS PlantType (" +
                "id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE)");
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    @Order(1)
    void testCreatePlantType() {
        CreatePlantTypeRequest request = CreatePlantTypeRequest.newBuilder()
                .setName("влажность")
                .build();

        PlantType created = plantTypeRepository.CreatePlantType(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getName()).isEqualTo("влажность");
    }

    @Test
    @Order(2)
    void testGetPlantTypeById_Found() {
        CreatePlantTypeRequest createRequest = CreatePlantTypeRequest.newBuilder()
                .setName("освещение")
                .build();
        PlantType created = plantTypeRepository.CreatePlantType(createRequest);

        GetPlantTypeRequest getRequest = GetPlantTypeRequest.newBuilder()
                .setId(created.getId())
                .build();

        PlantType fetched = plantTypeRepository.GetPlantTypeById(getRequest);

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("освещение");
    }

    @Test
    @Order(3)
    void testGetPlantTypeById_NotFound() {
        GetPlantTypeRequest getRequest = GetPlantTypeRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> plantTypeRepository.GetPlantTypeById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(4)
    void testUpdatePlantType_Success() {
        CreatePlantTypeRequest createRequest = CreatePlantTypeRequest.newBuilder()
                .setName("устойчивость к болезням")
                .build();
        PlantType created = plantTypeRepository.CreatePlantType(createRequest);

        PlantType updatedType = PlantType.newBuilder()
                .setId(created.getId())
                .setName("устойчивость")
                .build();

        PlantType updated = plantTypeRepository.UpdatePlantType(updatedType);

        assertThat(updated.getName()).isEqualTo("устойчивость");
    }

    @Test
    @Order(5)
    void testUpdatePlantType_NotFound() {
        PlantType plantType = PlantType.newBuilder()
                .setId(-1L)
                .setName("несуществующий тип")
                .build();

        assertThatThrownBy(() -> plantTypeRepository.UpdatePlantType(plantType))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(6)
    void testDeletePlantType_Success() {
        CreatePlantTypeRequest createRequest = CreatePlantTypeRequest.newBuilder()
                .setName("тип для удаления")
                .build();
        PlantType created = plantTypeRepository.CreatePlantType(createRequest);

        GetPlantTypeRequest deleteRequest = GetPlantTypeRequest.newBuilder()
                .setId(created.getId())
                .build();

        plantTypeRepository.DeletePlantType(deleteRequest);

        GetPlantTypeRequest getRequest = GetPlantTypeRequest.newBuilder()
                .setId(created.getId())
                .build();

        assertThatThrownBy(() -> plantTypeRepository.GetPlantTypeById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class);
    }

    @Test
    @Order(7)
    void testDeletePlantType_NotFound() {
        GetPlantTypeRequest deleteRequest = GetPlantTypeRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> plantTypeRepository.DeletePlantType(deleteRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(8)
    void testGetAllPlantTypes() {
        CreatePlantTypeRequest createRequest1 = CreatePlantTypeRequest.newBuilder()
                .setName("тип 1")
                .build();
        plantTypeRepository.CreatePlantType(createRequest1);

        CreatePlantTypeRequest createRequest2 = CreatePlantTypeRequest.newBuilder()
                .setName("тип 2")
                .build();
        plantTypeRepository.CreatePlantType(createRequest2);

        AllPlantTypesResponse allTypes = plantTypeRepository.GetAllPlantTypes();

        assertThat(allTypes.getPlantTypesCount()).isGreaterThanOrEqualTo(2);
    }
}
