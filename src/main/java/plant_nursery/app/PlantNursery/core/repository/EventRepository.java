package plant_nursery.app.PlantNursery.core.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import org.springframework.stereotype.Repository;
import plant_nursery.app.PlantNursery.core.repository.interfaces.IEventRepository;
import protobuf.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Repository
public class EventRepository implements IEventRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper для преобразования ResultSet в Event
    private static final RowMapper<EventShort> EVENT_ROW_MAPPER = new RowMapper<EventShort>() {
        @Override
        public EventShort mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Создаем Event
            EventShort.Builder eventBuilder = EventShort.newBuilder()
                    .setId(rs.getLong("id"))
                    .setEventTypeId(rs.getLong("type_id"))
                    .setCommentary(rs.getString("commentary"));

            return eventBuilder.build();
        }
    };

    @Override
    public EventShort CreateEvent(CreateEventRequest createEventRequest) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Event (type_id, commentary) VALUES (?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, createEventRequest.getEventTypeId());
            ps.setString(2, createEventRequest.getCommentary());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        GetEventRequest getEventRequest = GetEventRequest.newBuilder()
                .setId(id)
                .build();

        return GetEventById(getEventRequest);
    }

    @Override
    public EventShort GetEventById(GetEventRequest getEventRequest) {
        try {
            // не делать со звёздочкой! Придобавлении новых столбцов всё сломается!
            String sql = "SELECT * FROM Event Where id = ?";
            return jdbcTemplate.queryForObject(sql, EVENT_ROW_MAPPER, getEventRequest.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new RuntimeException("Event with id " + getEventRequest.getId() + " not found");
        }
    }

    @Override
    public EventShort UpdateEvent(EventShort event) {
        int updatedRows = jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Event SET type_id = ?, commentary = ? WHERE id = ?"
            );
            ps.setLong(1, event.getEventTypeId());
            ps.setString(2, event.getCommentary());
            ps.setLong(3, event.getId());
            return ps;
        });

        if (updatedRows == 0) {
            throw new RuntimeException("Event with id " + event.getId() + " not found");
        }

        GetEventRequest getEventRequest = GetEventRequest.newBuilder()
                .setId(event.getId())
                .build();

        return GetEventById(getEventRequest);
    }

    @Override
    public void DeleteEvent(DeleteEventRequest deleteEventRequest) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM Event WHERE id = ?",
                deleteEventRequest.getId()
        );

        if (deletedRows == 0) {
            throw new RuntimeException("Event with id " + deleteEventRequest.getId() + " not found");
        }
    }
}