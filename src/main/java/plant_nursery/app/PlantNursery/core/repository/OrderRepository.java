package plant_nursery.app.PlantNursery.core.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import plant_nursery.app.PlantNursery.core.exception.RepositoryArgumentException;
import plant_nursery.app.PlantNursery.core.repository.interfaces.IOrderRepository;
import protobuf.*;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Repository
public class OrderRepository implements IOrderRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);
    private static  JdbcTemplate jdbcTemplate;
    private static SectorRepository sectorRepository;

    public OrderRepository(JdbcTemplate jdbcTemplate, SectorRepository sectorRepository) {
        OrderRepository.jdbcTemplate = jdbcTemplate;
        OrderRepository.sectorRepository = sectorRepository;
    }

    private static final RowMapper<Order> ORDER_ROW_MAPPER = (resultSet, rowNum) -> {
        Order.Builder orderBuilder = Order.newBuilder();
        orderBuilder.setId(resultSet.getLong("id"));
        orderBuilder.setCompanyName(resultSet.getString("company_name"));
        orderBuilder.setStatus(resultSet.getString("status"));
        orderBuilder.setCommentary(resultSet.getString("commentary"));
        orderBuilder.setQuantity(resultSet.getInt("quantity"));

        if (resultSet.getLong("sector_plant_id") != 0) {
            orderBuilder.setSector(sectorRepository.getSectorWithPlantsById(
                (GetSectorRequest.newBuilder().setId(resultSet.getLong("sector_plant_id")).build()
                )));
        }
        return orderBuilder.build();
    };

    @Override
    public Order createOrder(CreateOrderRequest order) {
        int quantity = order.getQuantity() > 0 ? order.getQuantity() : 1;
        
        String insertOrderSql = """
            INSERT INTO "Order" (company_name, status, commentary, quantity)
            VALUES (?, ?, ?, ?)
            """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(insertOrderSql, new String[]{"id"});
            ps.setString(1, order.getCompanyName());
            ps.setString(2, order.getStatus());
            ps.setString(3, order.getCommentary());
            ps.setInt(4, quantity);
            return ps;
        }, keyHolder);

        Long sectorPlantId = order.getSector();
        String insertRelationSql = """
                INSERT INTO SectorPlantHasOrder (sector_plant_id, order_id)
                VALUES (?, ?)
                """;
        jdbcTemplate.update(insertRelationSql, sectorPlantId, keyHolder.getKey());

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return getOrderById(GetOrderRequest.newBuilder().setId(id).build());
    }

    @Override
    public Order getOrderById(GetOrderRequest request) {
        String sql = """
            SELECT o.id, o.company_name, o.status, o.commentary, o.quantity, spho.sector_plant_id
            FROM "Order" o
            LEFT JOIN SectorPlantHasOrder spho ON o.id = spho.order_id
            WHERE o.id = ?
            """;
        try {
            return jdbcTemplate.queryForObject(sql, ORDER_ROW_MAPPER, request.getId());
        } catch (Exception e) {
            throw new RepositoryArgumentException("Order with id " + request.getId() + " not found");
        }
    }

    @Override
    public Order updateOrder(UpdateOrderRequest order) {
        String sql = """
            UPDATE "Order"
            SET company_name = ?, status = ?, commentary = ?
            WHERE id = ?
            """;
        int updated = jdbcTemplate.update(
                sql,
                order.getCompanyName(),
                order.getStatus(),
                order.getCommentary(),
                order.getId()
        );

        if (updated == 0) {
            throw new RepositoryArgumentException("Order with id " + order.getId() + " not found");
        }
        return getOrderById(GetOrderRequest.newBuilder().setId(order.getId()).build());
    }

    @Override
    public void deleteOrder(GetOrderRequest request) {
        // Удаляем связи
        String deleteRelationSql = "DELETE FROM SectorPlantHasOrder WHERE order_id = ?";
        jdbcTemplate.update(deleteRelationSql, request.getId());

        // Удаляем заказ
        String deleteOrderSql = "DELETE FROM \"Order\" WHERE id = ?";
        int deleted = jdbcTemplate.update(deleteOrderSql, request.getId());

        if (deleted == 0) {
            throw new RepositoryArgumentException("Order with id " + request.getId() + " not found");
        }
    }

    @Override
    public GetAllOrdersResponse getAllOrders() {
        String sql = """
            SELECT o.id, o.company_name, o.status, o.commentary, o.quantity, spho.sector_plant_id
            FROM "Order" o
            LEFT JOIN SectorPlantHasOrder spho ON o.id = spho.order_id
            """;
        List<Order> orders = jdbcTemplate.query(sql, ORDER_ROW_MAPPER);

        log.info(Order.getDescriptor().toString());
        return GetAllOrdersResponse.newBuilder().addAllOrders(orders).build();
    }

    @Override
    public Order completeOrder(CompleteOrderRequest request) {
        String getOrderDataSql = """
            SELECT o.quantity, spho.sector_plant_id 
            FROM "Order" o
            JOIN SectorPlantHasOrder spho ON o.id = spho.order_id
            WHERE o.id = ?
            """;
        
        java.util.Map<String, Object> orderData = jdbcTemplate.queryForMap(getOrderDataSql, request.getId());
        
        Long sectorPlantId = (Long) orderData.get("sector_plant_id");
        Integer quantity = ((Number) orderData.get("quantity")).intValue();

        if (sectorPlantId == null) {
            throw new RepositoryArgumentException("Order " + request.getId() + " has no linked sector");
        }

        Integer checkSql = jdbcTemplate.queryForObject(
            "SELECT plant_count FROM SectorPlant WHERE id = ?",
            Integer.class,
            sectorPlantId
        );

        if (checkSql == null || checkSql < quantity) {
            throw new RepositoryArgumentException("Not enough plants on sector. Available: " + (checkSql != null ? checkSql : 0));
        }

        String updateOrderSql = """
            UPDATE "Order" SET status = 'COMPLETED' WHERE id = ?
            """;
        int updated = jdbcTemplate.update(updateOrderSql, request.getId());

        if (updated == 0) {
            throw new RepositoryArgumentException("Order with id " + request.getId() + " not found");
        }

        String decreasePlantsSql = """
            UPDATE SectorPlant SET plant_count = plant_count - ? WHERE id = ?
            """;
        jdbcTemplate.update(decreasePlantsSql, quantity, sectorPlantId);

        return getOrderById(GetOrderRequest.newBuilder().setId(request.getId()).build());
    }
}
