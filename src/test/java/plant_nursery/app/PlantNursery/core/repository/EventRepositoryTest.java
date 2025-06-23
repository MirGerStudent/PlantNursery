package plant_nursery.app.PlantNursery.core.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import protobuf.CreateEventRequest;

@Nested
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventRepositoryTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    EventRepository eventRepository;

    @BeforeEach
    void setUp() {

    }

    @Test
    void createEventGoodRequest() {
        eventRepository.CreateEvent(
                CreateEventRequest
                        .newBuilder()
                        .setEventTypeId(1L)
                        .setCommentary("LOL")
                        .build()
        );
    }

    @Test
    void getEventById() {
    }

    @Test
    void updateEvent() {
    }

    @Test
    void deleteEvent() {
    }
}