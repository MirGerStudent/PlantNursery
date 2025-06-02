package plant_nursery.app.PlantNursery.core.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import plant_nursery.app.PlantNursery.core.repository.interfaces.ISectorRepository;
import protobuf.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Repository
public class SectorRepository implements ISectorRepository {
    private final JdbcTemplate jdbcTemplate;
    private static PlantRepository plantRepository;

    public SectorRepository(JdbcTemplate jdbcTemplate, PlantRepository plantRepository) {
        this.jdbcTemplate = jdbcTemplate;
        SectorRepository.plantRepository = plantRepository;
    }

    private static final RowMapper<Sector> SECTOR_ROW_MAPPER = new RowMapper<Sector>() {
        @Override
        public Sector mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Создаем SectorService
            Sector.Builder eventBuilder = Sector.newBuilder()
                    .setId(rs.getLong("id"))
                    .setParentId(rs.getLong("parent_id"))
                    .setName(rs.getString("name"));

            return eventBuilder.build();
        }
    };

    private static final RowMapper<SectorWithPlants> SECTOR_WITH_PLANTS_ROW_MAPPER = (rs, rowNum) -> {
        SectorWithPlants.Builder builder = SectorWithPlants.newBuilder()
                .setId(rs.getLong("id"))
                .setParentId(rs.getLong("parent_id"))
                .setName(rs.getString("name"));

        if (rs.getLong("plant_id") != 0) {
            Plant plantedPlant = plantRepository.getPlantById(
                    GetPlantRequest.newBuilder().setId(rs.getLong("plant_id")).build()
            );
            builder.setPlantedPlant(plantedPlant);
            builder.setPlantCount(rs.getInt("plant_count"));
            Timestamp sqlTimestamp = rs.getTimestamp("planting_date");
            if (sqlTimestamp != null) {
                Instant instant = sqlTimestamp.toInstant();
                builder.setPlantingDate(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build());
            }
        }
        return builder.build();
    };

    private static final RowMapper<Event> EVENT_ROW_MAPPER = (rs, rowNum) -> {
        EventType eventType = EventType.newBuilder()
                .setId(rs.getLong("type_id"))
                .setName(rs.getString("type_name"))
                .build();
        java.sql.Timestamp sqlTimestamp = rs.getTimestamp("event_time");
        com.google.protobuf.Timestamp protoTimestamp = null;
        if (sqlTimestamp != null) {
            Instant instant = sqlTimestamp.toInstant();
            protoTimestamp = com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build();
        }
        Event.Builder eventBuilder = Event.newBuilder()
                .setId(rs.getLong("id"))
                .setType(eventType)
                .setCommentary(rs.getString("commentary"));
        if (protoTimestamp != null) {
            eventBuilder.setDate(protoTimestamp);
        }
        return eventBuilder.build();
    };

    @Override
    public Sector createSector(CreateSectorRequest sector) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Sector (parent_id, name) VALUES (?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, sector.getParentId());
            ps.setString(2, sector.getName());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        GetSectorRequest getSectorRequest = GetSectorRequest.newBuilder()
                .setId(id)
                .build();

        return getSectorById(getSectorRequest);
    }

    @Override
    public SectorWithPlants createSectorWithPlants(CreateSectorWithPlantsRequest createSectorWithPlantsRequest) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO SectorPlant (id, plant_id, plant_count, planting_date) VALUES (?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setLong(1, createSectorWithPlantsRequest.getId());
            ps.setLong(2, createSectorWithPlantsRequest.getPlantedPlantId());
            ps.setInt(3, createSectorWithPlantsRequest.getPlantCount());
            ps.setTimestamp(4, Timestamp.from(Instant.now()));
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        GetSectorRequest getSectorRequest = GetSectorRequest.newBuilder()
                .setId(id)
                .build();

        return getSectorWithPlantsById(getSectorRequest);
    }

    @Override
    public SectorEventsResponse getSectorEvents(GetSectorRequest getSectorRequest) {
        String eventSql = """
                    SELECT e.id, e.type_id, e.commentary, et.name AS type_name, se.event_time
                    FROM SectorEvent se
                    JOIN Event e ON se.event_id = e.id
                    JOIN EventType et ON e.type_id = et.id
                    WHERE se.sector_plant_id = ?
                    """;
        List<Event> events = jdbcTemplate.query(
                eventSql,
                EVENT_ROW_MAPPER,
                getSectorRequest.getId()
        );
        return SectorEventsResponse.newBuilder().addAllEvents(events).setSectorId(getSectorRequest.getId()).build();
    }

    @Override
    public Sector getSectorById(GetSectorRequest getSectorRequest) {
        try {
            String sql = "SELECT id, parent_id, name FROM Sector WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, SECTOR_ROW_MAPPER, getSectorRequest.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new RuntimeException("Sector with id " + getSectorRequest.getId() + " not found");
        }
    }

    @Override
    public SectorWithPlants getSectorWithPlantsById(GetSectorRequest getSectorRequest) {
        String sql = """
                SELECT s.id, s.parent_id, s.name, sp.id AS sector_plant_id, sp.plant_id, sp.plant_count, sp.planting_date
                FROM Sector s
                LEFT JOIN SectorPlant sp ON s.id = sp.id
                WHERE s.id = ?
                """;
        SectorWithPlants sectorWithPlants = jdbcTemplate.queryForObject(
                sql,
                SECTOR_WITH_PLANTS_ROW_MAPPER,
                getSectorRequest.getId()
        );

        if (sectorWithPlants == null) {
            throw new RuntimeException("Sector with id " + getSectorRequest.getId() + " not found");
        }

        // Получение событий, если есть посадка
//        if (sectorWithPlants.hasPlantedPlant()) {
            String eventSql = """
                    SELECT e.id, e.type_id, e.commentary, et.name AS type_name, se.event_time
                    FROM SectorEvent se
                    JOIN Event e ON se.event_id = e.id
                    JOIN EventType et ON e.type_id = et.id
                    WHERE se.sector_plant_id = ?
                    """;
            List<Event> events = jdbcTemplate.query(
                    eventSql,
                    EVENT_ROW_MAPPER,
                    getSectorRequest.getId()
            );
            sectorWithPlants.toBuilder().addAllEvents(events).build();
//        }
        return sectorWithPlants;
    }

    @Override
    public void addEventForSector(EventForSectorRequest eventForSectorRequest) {
        jdbcTemplate.update(
                "INSERT INTO SectorEvent (sector_plant_id, event_id, event_time) VALUES (?, ?, ?)",
                eventForSectorRequest.getSectorId(),
                eventForSectorRequest.getEventId(),
                Timestamp.from(Instant.now())
        );
    }

    @Override
    public Sector updateSector(Sector sector) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE Sector SET parent_id = ?, name = ? WHERE id = ?",
                sector.getParentId(),
                sector.getName(),
                sector.getId()
        );
        if (updatedRows == 0) {
            throw new RuntimeException("Sector with id " + sector.getId() + " not found");
        }
        return sector;
    }

    @Override
    public SectorWithPlants updateSectorWithPlants(UpdateSectorWithPlantsRequest updateSectorWithPlantsRequest) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE SectorPlant SET plant_id = ?, plant_count = ? WHERE id = ?",
                updateSectorWithPlantsRequest.getPlantedPlantId(),
                updateSectorWithPlantsRequest.getPlantCount(),
                updateSectorWithPlantsRequest.getId()
        );
        if (updatedRows == 0) {
            throw new RuntimeException("Sector with id " + updateSectorWithPlantsRequest.getId() + " not found");
        }

        GetSectorRequest getSectorRequest = GetSectorRequest.newBuilder()
                .setId(updateSectorWithPlantsRequest.getId())
                .build();
        return getSectorWithPlantsById(getSectorRequest);
    }

    @Override
    public void deleteSector(GetSectorRequest getSectorRequest) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM Sector WHERE id = ?",
                getSectorRequest.getId()
        );
        if (deletedRows == 0) {
            throw new RuntimeException("Sector with id " + getSectorRequest.getId() + " not found");
        }
    }

    @Override
    public void deleteSectorWithPlants(GetSectorRequest getSectorRequest) {
        deleteSector(getSectorRequest);
    }
}
