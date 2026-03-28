package plant_nursery.app.PlantNursery.core.repository;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import protobuf.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventTypeRepositoryTest {
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
    EventTypeRepository eventTypeRepository;

    @BeforeAll
    static void beforeAll() {
        postgresContainer.start();
    }

    @AfterAll
    static void afterAll() {
        postgresContainer.stop();
    }

    @Test
    @Order(1)
    void testCreateEventType() {
        CreateEventTypeRequest request = CreateEventTypeRequest.newBuilder()
                .setName("Новое событие 1")
                .build();

        EventType createdEventType = eventTypeRepository.CreateEventType(request);

        assertThat(createdEventType).isNotNull();
        assertThat(createdEventType.getId()).isPositive();
        assertThat(createdEventType.getName()).isEqualTo("Новое событие 1");
    }

    @Test
    @Order(2)
    void testGetEventTypeById_Found() {
        CreateEventTypeRequest createRequest = CreateEventTypeRequest.newBuilder()
                .setName("Новое событие 2")
                .build();
        EventType created = eventTypeRepository.CreateEventType(createRequest);

        GetEventTypeRequest getRequest = GetEventTypeRequest.newBuilder()
                .setId(created.getId())
                .build();

        EventType fetched = eventTypeRepository.GetEventTypeById(getRequest);

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Новое событие 2");
    }

    @Test
    @Order(3)
    void testGetEventTypeById_BadRequest() {
        GetEventTypeRequest getRequest = GetEventTypeRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> eventTypeRepository.GetEventTypeById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(4)
    void testUpdateEventType_Success() {
        CreateEventTypeRequest createRequest = CreateEventTypeRequest.newBuilder()
                .setName("Новое событие 3")
                .build();
        EventType created = eventTypeRepository.CreateEventType(createRequest);

        EventType updatedEventType = EventType.newBuilder()
                .setId(created.getId())
                .setName("Обновленное событие")
                .build();

        EventType updated = eventTypeRepository.UpdateEventType(updatedEventType);

        assertThat(updated.getName()).isEqualTo("Обновленное событие");
    }

    @Test
    @Order(5)
    void testUpdateEventType_BadRequest() {
        EventType eventType = EventType.newBuilder()
                .setId(-1L)
                .setName("Обновленное событие")
                .build();

        assertThatThrownBy(() -> eventTypeRepository.UpdateEventType(eventType))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(6)
    void testDeleteEventType_Success() {
        CreateEventTypeRequest createRequest = CreateEventTypeRequest.newBuilder()
                .setName("Новое событие 6")
                .build();
        EventType created = eventTypeRepository.CreateEventType(createRequest);

        GetEventTypeRequest deleteRequest = GetEventTypeRequest.newBuilder()
                .setId(created.getId())
                .build();

        eventTypeRepository.DeleteEventType(deleteRequest);

        GetEventTypeRequest getRequest = GetEventTypeRequest.newBuilder()
                .setId(created.getId())
                .build();

        assertThatThrownBy(() -> eventTypeRepository.GetEventTypeById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class);
    }

    @Test
    @Order(7)
    void testDeleteEventType_BadRequest() {
        GetEventTypeRequest deleteRequest = GetEventTypeRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> eventTypeRepository.DeleteEventType(deleteRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(8)
    void testGetAllEventTypes() {
        CreateEventTypeRequest createRequest1 = CreateEventTypeRequest.newBuilder()
                .setName("Событие 1")
                .build();
        EventType created1 = eventTypeRepository.CreateEventType(createRequest1);

        CreateEventTypeRequest createRequest2 = CreateEventTypeRequest.newBuilder()
                .setName("Событие 2")
                .build();
        EventType created2 = eventTypeRepository.CreateEventType(createRequest2);

        AllEventTypesResponse allEventTypes = eventTypeRepository.GetAllEventTypes();

        assertThat(allEventTypes.getEventTypesList().size()).isEqualTo(21);

        long id = allEventTypes.getEventTypesCount();

        assertThat(allEventTypes.getEventTypesList().get((int) (id-3))).isEqualTo(created1);
        assertThat(allEventTypes.getEventTypesList().get((int) (id-2))).isEqualTo(created2);
    }
}
