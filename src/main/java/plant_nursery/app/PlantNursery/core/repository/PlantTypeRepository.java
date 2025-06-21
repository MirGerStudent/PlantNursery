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
import plant_nursery.app.PlantNursery.core.repository.interfaces.IPlantTypeRepository;
import protobuf.AllPlantTypesResponse;
import protobuf.CreatePlantTypeRequest;
import protobuf.GetPlantTypeRequest;
import protobuf.PlantType;

@Repository
public class PlantTypeRepository implements IPlantTypeRepository {
    private final JdbcTemplate jdbcTemplate;

    public PlantTypeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<PlantType> PLANT_TYPE_ROW_MAPPER = new RowMapper<PlantType>() {
        @Override
        public PlantType mapRow(ResultSet rs, int rowNum) throws SQLException {
            return PlantType.newBuilder()
                    .setId(rs.getLong("id"))
                    .setName(rs.getString("name"))
                    .build();
        }
    };

    @Override
    public PlantType CreatePlantType(CreatePlantTypeRequest createPlantTypeRequest) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO PlantType (name) VALUES (?)",
                    new String[]{"id"}
            );
            ps.setString(1, createPlantTypeRequest.getName());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        GetPlantTypeRequest getPlantTypeRequest = GetPlantTypeRequest.newBuilder()
                .setId(id)
                .build();

        return GetPlantTypeById(getPlantTypeRequest);
    }

    @Override
    public PlantType UpdatePlantType(PlantType plantType) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE PlantType SET name = ? WHERE id = ?",
                plantType.getName(),
                plantType.getId()
        );

        if (updatedRows == 0) {
            throw new RepositoryArgumentException("PlantType with id " + plantType.getId() + " not found");
        }

        GetPlantTypeRequest getPlantTypeRequest = GetPlantTypeRequest.newBuilder()
                .setId(plantType.getId())
                .build();

        return GetPlantTypeById(getPlantTypeRequest);
    }

    @Override
    public void DeletePlantType(GetPlantTypeRequest getPlantTypeRequest) {
        int deletedRows = jdbcTemplate.update(
                "DELETE FROM PlantType WHERE id = ?",
                getPlantTypeRequest.getId()
        );

        if (deletedRows == 0) {
            throw new RepositoryArgumentException("PlantType with id " + getPlantTypeRequest.getId() + " not found");
        }
    }

    @Override
    public AllPlantTypesResponse GetAllPlantTypes() {
        String sql = "SELECT id, name FROM PlantType ORDER BY name";
        List<PlantType> plantTypes = jdbcTemplate.query(sql, PLANT_TYPE_ROW_MAPPER);

        return AllPlantTypesResponse.newBuilder()
                .addAllPlantTypes(plantTypes)
                .build();
    }

    @Override
    public PlantType GetPlantTypeById(GetPlantTypeRequest getPlantTypeRequest) {
        try {
            String sql = "SELECT id, name FROM PlantType WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, PLANT_TYPE_ROW_MAPPER, getPlantTypeRequest.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new RepositoryArgumentException("PlantType with id " + getPlantTypeRequest.getId() + " not found");
        }
    }
}
