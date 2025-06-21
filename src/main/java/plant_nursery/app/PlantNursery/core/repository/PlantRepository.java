package plant_nursery.app.PlantNursery.core.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import plant_nursery.app.PlantNursery.core.exception.RepositoryDataException;
import plant_nursery.app.PlantNursery.core.repository.interfaces.IPlantRepository;
import protobuf.*;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class PlantRepository implements IPlantRepository {
    private final JdbcTemplate jdbcTemplate;

    public PlantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Plant save(CreatePlantRequest createPlantRequest) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Plant (height_stem_min, height_stem_max, width_stem_min, width_stem_max, spice, sort, description) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    new String[]{"id"}
            );
            ps.setInt(1, createPlantRequest.getHeightStemMin());
            ps.setInt(2, createPlantRequest.getHeightStemMax());
            ps.setInt(3, createPlantRequest.getWidthStemMin());
            ps.setInt(4, createPlantRequest.getWidthStemMax());
            ps.setString(5, createPlantRequest.getSpice());
            ps.setString(6, createPlantRequest.getSort());
            ps.setString(7, createPlantRequest.getDescription());
            return ps;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        GetPlantRequest getPlantRequest = GetPlantRequest.newBuilder()
                .setId(id)
                .build();
        return getPlantById(getPlantRequest);
    }

    @Override
    public Plant getPlantById(GetPlantRequest getPlantRequest) {
        Map<String, String> plantTypes = new HashMap<>();

        String sql = """
            SELECT pt.name, pht.type_value
            FROM Plant p
            JOIN PlantHasType pht ON p.id = pht.plant_id
            JOIN PlantType pt ON pht.type_id = pt.id
            WHERE p.id = ?
        """;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, getPlantRequest.getId());

        for (Map<String, Object> row : rows ) {
            plantTypes.put((String) row.get("name"), (String) row.get("type_value"));
        }


        try {
            return jdbcTemplate.queryForObject("SELECT id, " +
                            "height_stem_min, " +
                            "height_stem_max, " +
                            "width_stem_min, " +
                            "width_stem_max, " +
                            "spice, sort, description FROM Plant WHERE id = ?",
                    (rs, rowNum) -> {
                        Plant.Builder plant = Plant.newBuilder().setId(rs.getLong("id"))
                                .setHeightStemMin(rs.getInt("height_stem_min"))
                                .setHeightStemMax(rs.getInt("height_stem_max"))
                                .setWidthStemMin(rs.getInt("width_stem_min"))
                                .setWidthStemMax(rs.getInt("width_stem_max"))
                                .setSpice(rs.getString("spice"))
                                .setSort(rs.getString("sort"))
                                .setDescription(rs.getString("description"));

                        if (!plantTypes.isEmpty()) {
                            plant.putAllCharacteristics(plantTypes);
                        }

                        return plant.build();
                    }
                    , getPlantRequest.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new RepositoryArgumentException("Plant with id " + getPlantRequest.getId() + " not found");
        }
    }

    @Override
    public Plant updatePlant(UpdatePlantRequest updatePlantRequest) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE Plant SET " +
                        "height_stem_min = ?, " +
                        "height_stem_max = ?, " +
                        "width_stem_min = ?, " +
                        "width_stem_max = ?, " +
                        "spice = ?, " +
                        "sort = ?, " +
                        "description = ? " +
                        "WHERE id = ?",
                updatePlantRequest.getHeightStemMin(),
                updatePlantRequest.getHeightStemMax(),
                updatePlantRequest.getWidthStemMin(),
                updatePlantRequest.getWidthStemMax(),
                updatePlantRequest.getSpice(),
                updatePlantRequest.getSort(),
                updatePlantRequest.getDescription(),
                updatePlantRequest.getId() // Важно: используем ID из запроса для обновления
        );

        // Проверяем, была ли запись обновлена
        if (updatedRows == 0) {
            throw new RepositoryArgumentException("Plant with id " + updatePlantRequest.getId() + " not found");
        }
        GetPlantRequest getPlantRequest = GetPlantRequest.newBuilder()
                .setId(updatePlantRequest.getId())
                .build();
        return getPlantById(getPlantRequest);
    }

    @Override
    public Plant updatePlantCharacteristics(UpdatePlantCharacteristicsRequest updatePlantCharacteristicsRequest) {
        try {
            for (Map.Entry<String, String> elem : updatePlantCharacteristicsRequest.getCharacteristicsMap().entrySet()) {
                int updatedRows = jdbcTemplate.update(
                        "UPDATE PlantHasType SET type_value = ? WHERE plant_id = ? AND type_id = ?",
                        elem.getValue(),
                        updatePlantCharacteristicsRequest.getId(),
                        Long.parseLong(elem.getKey())
                );

                if (updatedRows == 0) {
                    jdbcTemplate.update(
                            "INSERT INTO PlantHasType (plant_id, type_id, type_value) VALUES (?, ?, ?)",
                            updatePlantCharacteristicsRequest.getId(),
                            Long.parseLong(elem.getKey()),
                            elem.getValue()
                    );
                }
            }

            GetPlantRequest getPlantRequest = GetPlantRequest.newBuilder()
                    .setId(updatePlantCharacteristicsRequest.getId())
                    .build();
            return getPlantById(getPlantRequest);
        } catch (Exception e) {
            throw new RepositoryDataException("Ошибка сериализации характеристик", e);
        }
    }

    @Override
    public Plant updatePlantCharacteristic(UpdatePlantCharacteristicRequest updatePlantCharacteristicRequest) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE PlantHasType SET type_value = ? WHERE plant_id = ? AND type_id = ?",
                updatePlantCharacteristicRequest.getTypeValue(),
                updatePlantCharacteristicRequest.getPlantId(),
                updatePlantCharacteristicRequest.getPlantType()
        );

        if (updatedRows == 0) {
            jdbcTemplate.update(
                    "INSERT INTO PlantHasType (plant_id, type_id, type_value) VALUES (?, ?, ?)",
                    updatePlantCharacteristicRequest.getPlantId(),
                    updatePlantCharacteristicRequest.getPlantType(),
                    updatePlantCharacteristicRequest.getTypeValue());
        }

        GetPlantRequest getPlantRequest = GetPlantRequest.newBuilder()
                .setId(updatePlantCharacteristicRequest.getPlantId())
                .build();
        return getPlantById(getPlantRequest);
    }

    @Override
    public void deletePant(DeletePlantRequest deletePlantRequest) {
        int deletedRows = jdbcTemplate.update("DELETE FROM Plant WHERE id = ?", deletePlantRequest.getId());

        if (deletedRows == 0) {
            throw new RepositoryArgumentException("Plant with id " + deletePlantRequest.getId() + " not found");
        }
    }
}
