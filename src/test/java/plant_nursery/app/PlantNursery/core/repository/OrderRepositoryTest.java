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
import protobuf.CompleteOrderRequest;
import protobuf.CreateOrderRequest;
import protobuf.GetOrderRequest;
import protobuf.GetSectorRequest;
import protobuf.Order;
import protobuf.Sector;
import protobuf.SectorWithPlants;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderRepositoryTest {
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
    OrderRepository orderRepository;

    @Autowired
    PlantRepository plantRepository;

    @Autowired
    SectorRepository sectorRepository;

    @Autowired
    EventTypeRepository eventTypeRepository;

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @BeforeEach
    void setUp() {
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
    void testCreateOrder() {
        protobuf.CreatePlantRequest plantRequest = protobuf.CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Тестовое растение")
                .setSort("Тестовый сорт")
                .setDescription("Для теста")
                .build();
        protobuf.Plant createdPlant = plantRepository.save(plantRequest);

        Sector parentSector = sectorRepository.createSector(
                protobuf.CreateSectorRequest.newBuilder()
                        .setName("Тестовый сектор")
                        .setParentId(0)
                        .build()
        );

        jdbcTemplate.update("INSERT INTO SectorPlant (plant_id, plant_count, planting_date) VALUES (?, ?, NOW())",
                createdPlant.getId(), 100);

        Long sectorPlantId = jdbcTemplate.queryForObject("SELECT id FROM SectorPlant WHERE plant_id = ?", Long.class, createdPlant.getId());

        CreateOrderRequest request = CreateOrderRequest.newBuilder()
                .setStatus("NEW")
                .setCommentary("Тестовый заказ")
                .setCompanyName("Тестовая компания")
                .setSector(sectorPlantId)
                .setQuantity(10)
                .build();

        Order created = orderRepository.createOrder(request);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getCompanyName()).isEqualTo("Тестовая компания");
        assertThat(created.getStatus()).isEqualTo("NEW");
        assertThat(created.getQuantity()).isEqualTo(10);
    }

    @Test
    void testGetOrderById_Found() {
        protobuf.CreatePlantRequest plantRequest = protobuf.CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Растение 2")
                .setSort("Сорт 2")
                .setDescription("Описание 2")
                .build();
        protobuf.Plant createdPlant = plantRepository.save(plantRequest);

        Sector parentSector = sectorRepository.createSector(
                protobuf.CreateSectorRequest.newBuilder()
                        .setName("Сектор 2")
                        .setParentId(0)
                        .build()
        );

        jdbcTemplate.update("INSERT INTO SectorPlant (plant_id, plant_count, planting_date) VALUES (?, ?, NOW())",
                createdPlant.getId(), 50);

        Long sectorPlantId = jdbcTemplate.queryForObject("SELECT id FROM SectorPlant WHERE plant_id = ?", Long.class, createdPlant.getId());

        CreateOrderRequest createRequest = CreateOrderRequest.newBuilder()
                .setStatus("NEW")
                .setCommentary("Заказ 2")
                .setCompanyName("Компания 2")
                .setSector(sectorPlantId)
                .setQuantity(5)
                .build();
        Order created = orderRepository.createOrder(createRequest);

        GetOrderRequest getRequest = GetOrderRequest.newBuilder()
                .setId(created.getId())
                .build();

        Order fetched = orderRepository.getOrderById(getRequest);

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getCompanyName()).isEqualTo("Компания 2");
    }

    @Test
    void testGetOrderById_NotFound() {
        GetOrderRequest getRequest = GetOrderRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> orderRepository.getOrderById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void testCompleteOrder_Success() {
        protobuf.CreatePlantRequest plantRequest = protobuf.CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Растение 3")
                .setSort("Сорт 3")
                .setDescription("Описание 3")
                .build();
        protobuf.Plant createdPlant = plantRepository.save(plantRequest);

        Sector parentSector = sectorRepository.createSector(
                protobuf.CreateSectorRequest.newBuilder()
                        .setName("Сектор 3")
                        .setParentId(0)
                        .build()
        );

        jdbcTemplate.update("INSERT INTO SectorPlant (plant_id, plant_count, planting_date) VALUES (?, ?, NOW())",
                createdPlant.getId(), 100);

        Long sectorPlantId = jdbcTemplate.queryForObject("SELECT id FROM SectorPlant WHERE plant_id = ?", Long.class, createdPlant.getId());

        CreateOrderRequest createRequest = CreateOrderRequest.newBuilder()
                .setStatus("NEW")
                .setCommentary("Заказ для выполнения")
                .setCompanyName("Компания выполнения")
                .setSector(sectorPlantId)
                .setQuantity(5)
                .build();
        Order created = orderRepository.createOrder(createRequest);

        Integer plantCountBefore = jdbcTemplate.queryForObject(
                "SELECT plant_count FROM SectorPlant WHERE id = ?", Integer.class, sectorPlantId);

        CompleteOrderRequest completeRequest = CompleteOrderRequest.newBuilder()
                .setId(created.getId())
                .build();

        Order completed = orderRepository.completeOrder(completeRequest);

        Integer plantCountAfter = jdbcTemplate.queryForObject(
                "SELECT plant_count FROM SectorPlant WHERE id = ?", Integer.class, sectorPlantId);

        assertThat(completed.getStatus()).isEqualTo("COMPLETED");
        assertThat(plantCountBefore - plantCountAfter).isEqualTo(5);
    }

    @Test
    void testCompleteOrder_NotEnoughPlants() {
        protobuf.CreatePlantRequest plantRequest = protobuf.CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Растение 4")
                .setSort("Сорт 4")
                .setDescription("Описание 4")
                .build();
        protobuf.Plant createdPlant = plantRepository.save(plantRequest);

        Sector parentSector = sectorRepository.createSector(
                protobuf.CreateSectorRequest.newBuilder()
                        .setName("Сектор 4")
                        .setParentId(0)
                        .build()
        );

        jdbcTemplate.update("INSERT INTO SectorPlant (plant_id, plant_count, planting_date) VALUES (?, ?, NOW())",
                createdPlant.getId(), 2);

        Long sectorPlantId = jdbcTemplate.queryForObject("SELECT id FROM SectorPlant WHERE plant_id = ?", Long.class, createdPlant.getId());

        CreateOrderRequest createRequest = CreateOrderRequest.newBuilder()
                .setStatus("NEW")
                .setCommentary("Большой заказ")
                .setCompanyName("Компания")
                .setSector(sectorPlantId)
                .setQuantity(100)
                .build();
        Order created = orderRepository.createOrder(createRequest);

        CompleteOrderRequest completeRequest = CompleteOrderRequest.newBuilder()
                .setId(created.getId())
                .build();

        assertThatThrownBy(() -> orderRepository.completeOrder(completeRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("Not enough plants");
    }

    @Test
    void testDeleteOrder_Success() {
        protobuf.CreatePlantRequest plantRequest = protobuf.CreatePlantRequest.newBuilder()
                .setHeightStemMin(1)
                .setHeightStemMax(3)
                .setWidthStemMin(1)
                .setWidthStemMax(1)
                .setSpice("Растение 5")
                .setSort("Сорт 5")
                .setDescription("Описание 5")
                .build();
        protobuf.Plant createdPlant = plantRepository.save(plantRequest);

        Sector parentSector = sectorRepository.createSector(
                protobuf.CreateSectorRequest.newBuilder()
                        .setName("Сектор 5")
                        .setParentId(0)
                        .build()
        );

        jdbcTemplate.update("INSERT INTO SectorPlant (plant_id, plant_count, planting_date) VALUES (?, ?, NOW())",
                createdPlant.getId(), 10);

        Long sectorPlantId = jdbcTemplate.queryForObject("SELECT id FROM SectorPlant WHERE plant_id = ?", Long.class, createdPlant.getId());

        CreateOrderRequest createRequest = CreateOrderRequest.newBuilder()
                .setStatus("NEW")
                .setCommentary("Заказ для удаления")
                .setCompanyName("Компания удаления")
                .setSector(sectorPlantId)
                .setQuantity(1)
                .build();
        Order created = orderRepository.createOrder(createRequest);

        GetOrderRequest deleteRequest = GetOrderRequest.newBuilder()
                .setId(created.getId())
                .build();

        orderRepository.deleteOrder(deleteRequest);

        assertThatThrownBy(() -> orderRepository.getOrderById(deleteRequest))
                .isInstanceOf(RepositoryArgumentException.class);
    }
}
