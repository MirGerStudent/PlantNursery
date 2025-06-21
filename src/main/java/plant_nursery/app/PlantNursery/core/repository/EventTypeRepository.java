package plant_nursery.app.PlantNursery.core.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import plant_nursery.app.PlantNursery.core.repository.interfaces.IEventTypeRepository;
import protobuf.AllEventTypesResponse;
import protobuf.CreateEventTypeRequest;
import protobuf.GetEventTypeRequest;
import protobuf.EventType;

@Repository
public class EventTypeRepository implements IEventTypeRepository {
    private final JdbcTemplate jdbcTemplate;

    public EventTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<EventType> EVENT_TYPE_ROW_MAPPER = new RowMapper<EventType>() {
        @Override
        public EventType mapRow(ResultSet rs, int rowNum) throws SQLException {
            return EventType.newBuilder()
                    .setId(rs.getLong("id"))
                    .setName(rs.getString("name"))
                    .build();
        }
    };

    @Override
    public EventType CreateEventType(CreateEventTypeRequest createEventTypeRequest) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO EventType (name) VALUES (?)",
                    new String[]{"id"}
            );
            ps.setString(1, createEventTypeRequest.getName());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        GetEventTypeRequest getEventTypeRequest = GetEventTypeRequest.newBuilder()
                .setId(id)
                .build();

        return GetEventTypeById(getEventTypeRequest);
    }

    @Override
    public EventType UpdateEventType(EventType EventType) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE EventType SET name = ? WHERE id = ?",
                EventType.getName(),
                EventType.getId()
        );

        if (updatedRows == 0) {
            throw new RepositoryArgumentException("EventType with id " + EventType.getId() + " not found");
        }

        GetEventTypeRequest getEventTypeRequest = GetEventTypeRequest.newBuilder()
                .setId(EventType.getId())
                .build();

        return GetEventTypeById(getEventTypeRequest);
    }

    @Override
    public void DeleteEventType(GetEventTypeRequest getEventTypeRequest) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM EventType WHERE id = ?",
                getEventTypeRequest.getId()
        );

        if (deletedRows == 0) {
            throw new RepositoryArgumentException("EventType with id " + getEventTypeRequest.getId() + " not found");
        }
    }

    @Override
    public AllEventTypesResponse GetAllEventTypes() {
        String sql = "SELECT id, name FROM EventType ORDER BY name";
        List<EventType> EventTypes = jdbcTemplate.query(sql, EVENT_TYPE_ROW_MAPPER);

        return AllEventTypesResponse.newBuilder()
                .addAllEventTypes(EventTypes)
                .build();
    }

    @Override
    public EventType GetEventTypeById(GetEventTypeRequest getEventTypeRequest) {
        try {
            String sql = "SELECT id, name FROM EventType WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, EVENT_TYPE_ROW_MAPPER, getEventTypeRequest.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new RepositoryArgumentException("EventType with id " + getEventTypeRequest.getId() + " not found");
        }
    }
}
