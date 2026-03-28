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
import protobuf.CreatePlantRequest;
import protobuf.DeletePlantRequest;
import protobuf.Empty;
import protobuf.GetPlantRequest;
import protobuf.Plant;
import protobuf.Plants;
import protobuf.UpdatePlantCharacteristicRequest;
import protobuf.UpdatePlantRequest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PlantRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("PlantNurseryTest");

    static JdbcTemplate testJdbcTemplate;

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
    PlantRepository plantRepository;

    @Autowired
    PlantTypeRepository plantTypeRepository;

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @BeforeEach
    void setUp() {
        testJdbcTemplate = jdbcTemplate;
        initializeDatabase();
    }

    void initializeDatabase() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS \"User\" CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS PlantType CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS EventType CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS Sector CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS Plant CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS PlantHasType CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS SectorPlant CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS SectorEvent CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS \"Order\" CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS SectorPlantHasOrder CASCADE");

        jdbcTemplate.execute("CREATE TABLE \"User\" (" +
                "id BIGSERIAL PRIMARY KEY, role VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL)");
        
        jdbcTemplate.execute("CREATE TABLE PlantType (" +
                "id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE)");
        
        jdbcTemplate.execute("CREATE TABLE EventType (" +
                "id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL UNIQUE)");
        
        jdbcTemplate.execute("CREATE TABLE Sector (" +
                "id BIGSERIAL PRIMARY KEY, parent_id BIGINT REFERENCES Sector(id) ON DELETE SET NULL DEFERRABLE INITIALLY DEFERRED, name VARCHAR(255) NOT NULL UNIQUE)");
        
        jdbcTemplate.execute("CREATE TABLE Plant (" +
                "id BIGSERIAL PRIMARY KEY, height_stem_min INT, height_stem_max INT, " +
                "width_stem_min INT, width_stem_max INT, spice VARCHAR(255) NOT NULL, " +
                "sort VARCHAR(255) NOT NULL, description TEXT)");
        
        jdbcTemplate.execute("CREATE TABLE PlantHasType (" +
                "plant_id BIGINT NOT NULL REFERENCES Plant(id), type_id BIGINT NOT NULL REFERENCES PlantType(id), " +
                "type_value VARCHAR(255), PRIMARY KEY (plant_id, type_id))");
        
        jdbcTemplate.execute("CREATE TABLE SectorPlant (" +
                "id BIGSERIAL PRIMARY KEY, plant_id BIGINT NOT NULL REFERENCES Plant(id), " +
                "plant_count INT NOT NULL, planting_date TIMESTAMP NOT NULL)");
        
        jdbcTemplate.execute("CREATE TABLE SectorEvent (" +
                "id BIGSERIAL PRIMARY KEY, sector_plant_id BIGINT NOT NULL REFERENCES SectorPlant(id), " +
                "event_type_id BIGINT NOT NULL REFERENCES EventType(id), commentary TEXT, event_time TIMESTAMP NOT NULL)");
        
        jdbcTemplate.execute("CREATE TABLE \"Order\" (" +
                "id BIGSERIAL PRIMARY KEY, company_name VARCHAR(255) NOT NULL, " +
                "status VARCHAR(255) NOT NULL, commentary TEXT, quantity INT NOT NULL DEFAULT 1)");
        
        jdbcTemplate.execute("CREATE TABLE SectorPlantHasOrder (" +
                "sector_plant_id BIGINT NOT NULL REFERENCES SectorPlant(id), " +
                "order_id BIGINT NOT NULL REFERENCES \"Order\"(id), PRIMARY KEY (sector_plant_id, order_id))");

        jdbcTemplate.execute("INSERT INTO PlantType (name) VALUES ('тип почвы'), ('морозостойкость') ON CONFLICT DO NOTHING");
        
        jdbcTemplate.execute("INSERT INTO Sector (id, parent_id, name) VALUES (0, NULL, 'Корень')");
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    @Order(1)
    void testCreatePlant() {
        CreatePlantRequest request = CreatePlantRequest.newBuilder()
                .setHeightStemMin(5)
                .setHeightStemMax(15)
                .setWidthStemMin(2)
                .setWidthStemMax(5)
                .setSpice("Дуб")
                .setSort("Дуб черешчатый")
                .setDescription("Мощное дерево")
                .build();

        Plant created = plantRepository.save(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getSpice()).isEqualTo("Дуб");
        assertThat(created.getSort()).isEqualTo("Дуб черешчатый");
    }

    @Test
    @Order(2)
    void testGetPlantById_Found() {
        CreatePlantRequest createRequest = CreatePlantRequest.newBuilder()
                .setHeightStemMin(3)
                .setHeightStemMax(10)
                .setWidthStemMin(1)
                .setWidthStemMax(3)
                .setSpice("Берёза")
                .setSort("Берёза повислая")
                .setDescription("Белоствольное дерево")
                .build();
        Plant created = plantRepository.save(createRequest);

        GetPlantRequest getRequest = GetPlantRequest.newBuilder()
                .setId(created.getId())
                .build();

        Plant fetched = plantRepository.getPlantById(getRequest);

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getSpice()).isEqualTo("Берёза");
    }

    @Test
    @Order(3)
    void testGetPlantById_NotFound() {
        GetPlantRequest getRequest = GetPlantRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> plantRepository.getPlantById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(4)
    void testUpdatePlant_Success() {
        CreatePlantRequest createRequest = CreatePlantRequest.newBuilder()
                .setHeightStemMin(2)
                .setHeightStemMax(8)
                .setWidthStemMin(1)
                .setWidthStemMax(2)
                .setSpice("Сосна")
                .setSort("Сосна обыкновенная")
                .setDescription("Хвойное дерево")
                .build();
        Plant created = plantRepository.save(createRequest);

        UpdatePlantRequest updateRequest = UpdatePlantRequest.newBuilder()
                .setId(created.getId())
                .setHeightStemMin(3)
                .setHeightStemMax(12)
                .setWidthStemMin(2)
                .setWidthStemMax(4)
                .setSpice("Сосна")
                .setSort("Сосна крымская")
                .setDescription("Обновлённое описание")
                .build();

        Plant updated = plantRepository.updatePlant(updateRequest);

        assertThat(updated.getSort()).isEqualTo("Сосна крымская");
        assertThat(updated.getHeightStemMin()).isEqualTo(3);
    }

    @Test
    @Order(5)
    void testUpdatePlant_NotFound() {
        UpdatePlantRequest updateRequest = UpdatePlantRequest.newBuilder()
                .setId(-1L)
                .setHeightStemMin(3)
                .setHeightStemMax(12)
                .setWidthStemMin(2)
                .setWidthStemMax(4)
                .setSpice("Сосна")
                .setSort("Сосна крымская")
                .setDescription("Обновлённое описание")
                .build();

        assertThatThrownBy(() -> plantRepository.updatePlant(updateRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(6)
    void testDeletePlant_Success() {
        CreatePlantRequest createRequest = CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(5)
                .setWidthStemMin(1)
                .setWidthStemMax(2)
                .setSpice("Ель")
                .setSort("Ель обыкновенная")
                .setDescription("Новогоднее дерево")
                .build();
        Plant created = plantRepository.save(createRequest);

        DeletePlantRequest deleteRequest = DeletePlantRequest.newBuilder()
                .setId(created.getId())
                .build();

        plantRepository.deletePant(deleteRequest);

        GetPlantRequest getRequest = GetPlantRequest.newBuilder()
                .setId(created.getId())
                .build();

        assertThatThrownBy(() -> plantRepository.getPlantById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class);
    }

    @Test
    @Order(7)
    void testDeletePlant_NotFound() {
        DeletePlantRequest deleteRequest = DeletePlantRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> plantRepository.deletePant(deleteRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(8)
    void testGetAllPlants() {
        CreatePlantRequest createRequest1 = CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Липа")
                .setSort("Липа мелколистная")
                .setDescription("Медонос")
                .build();
        plantRepository.save(createRequest1);

        CreatePlantRequest createRequest2 = CreatePlantRequest.newBuilder()
                .setHeightStemMin(2)
                .setHeightStemMax(6)
                .setWidthStemMin(1)
                .setWidthStemMax(2)
                .setSpice("Клён")
                .setSort("Клён остролистный")
                .setDescription("Декоративное дерево")
                .build();
        plantRepository.save(createRequest2);

        Plants allPlants = plantRepository.getAllPlants(Empty.newBuilder().build());

        assertThat(allPlants.getPlantsList().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @Order(9)
    void testUpdatePlantCharacteristic() {
        CreatePlantRequest createRequest = CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Рябина")
                .setSort("Рябина обыкновенная")
                .setDescription("Ягодное дерево")
                .build();
        Plant created = plantRepository.save(createRequest);

        UpdatePlantCharacteristicRequest updateRequest = UpdatePlantCharacteristicRequest.newBuilder()
                .setPlantId(created.getId())
                .setPlantType(1L)
                .setTypeValue("суглинок")
                .build();

        Plant updated = plantRepository.updatePlantCharacteristic(updateRequest);

        assertThat(updated.getCharacteristicsCount()).isGreaterThan(0);
        assertThat(updated.getCharacteristicsMap().get("тип почвы")).isEqualTo("суглинок");
    }
}
