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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EventRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("PlantNurseryTest");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.platform", () -> "postgresql");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");
        registry.add("spring.sql.init.data-locations", () -> "classpath:data.sql");
        registry.add("spring.sql.init.continue-on-error", () -> "true");
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EventRepository eventRepository;

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
    void testCreateEvent() {
        CreateEventRequest request = CreateEventRequest.newBuilder()
                .setEventTypeId(1L)
                .setCommentary("4 литра")
                .build();

        EventShort createdEvent = eventRepository.CreateEvent(request);

        assertThat(createdEvent).isNotNull();
        assertThat(createdEvent.getId()).isPositive();
        assertThat(createdEvent.getEventTypeId()).isEqualTo(1L);
        assertThat(createdEvent.getCommentary()).isEqualTo("4 литра");
    }

    @Test
    @Order(2)
    void testGetEventById_Found() {
        CreateEventRequest createRequest = CreateEventRequest.newBuilder()
                .setEventTypeId(2L)
                .setCommentary("2 литра")
                .build();
        EventShort created = eventRepository.CreateEvent(createRequest);

        GetEventRequest getRequest = GetEventRequest.newBuilder()
                .setId(created.getId())
                .build();

        EventShort fetched = eventRepository.GetEventById(getRequest);

        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getEventTypeId()).isEqualTo(2L);
        assertThat(fetched.getCommentary()).isEqualTo("2 литра");
    }

    @Test
    @Order(3)
    void testGetEventById_BadRequest() {
        GetEventRequest getRequest = GetEventRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> eventRepository.GetEventById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(4)
    void testUpdateEvent_Success() {
        CreateEventRequest createRequest = CreateEventRequest.newBuilder()
                .setEventTypeId(2L)
                .setCommentary("2 литра")
                .build();
        EventShort created = eventRepository.CreateEvent(createRequest);

        EventShort updatedEvent = EventShort.newBuilder()
                .setId(created.getId())
                .setEventTypeId(1L)
                .setCommentary("3 литра")
                .build();

        EventShort updated = eventRepository.UpdateEvent(updatedEvent);

        assertThat(updated.getEventTypeId()).isEqualTo(1L);
        assertThat(updated.getCommentary()).isEqualTo("3 литра");
    }

    @Test
    @Order(5)
    void testUpdateEvent_BadRequest() {
        EventShort event = EventShort.newBuilder()
                .setId(-1L)
                .setEventTypeId(1L)
                .setCommentary("3 литра")
                .build();

        assertThatThrownBy(() -> eventRepository.UpdateEvent(event))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @Order(6)
    void testDeleteEvent_Success() {
        CreateEventRequest createRequest = CreateEventRequest.newBuilder()
                .setEventTypeId(1L)
                .setCommentary("3 литра")
                .build();
        EventShort created = eventRepository.CreateEvent(createRequest);

        DeleteEventRequest deleteRequest = DeleteEventRequest.newBuilder()
                .setId(created.getId())
                .build();

        eventRepository.DeleteEvent(deleteRequest);

        GetEventRequest getRequest = GetEventRequest.newBuilder()
                .setId(created.getId())
                .build();

        assertThatThrownBy(() -> eventRepository.GetEventById(getRequest))
                .isInstanceOf(RepositoryArgumentException.class);
    }

    @Test
    @Order(7)
    void testDeleteEvent_BadRequest() {
        DeleteEventRequest deleteRequest = DeleteEventRequest.newBuilder()
                .setId(-1L)
                .build();

        assertThatThrownBy(() -> eventRepository.DeleteEvent(deleteRequest))
                .isInstanceOf(RepositoryArgumentException.class)
                .hasMessageContaining("not found");
    }
}