package plant_nursery.app.PlantNursery.core.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
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

        if (resultSet.getLong("sector_plant_id") != 0) {
            orderBuilder.setSector(sectorRepository.getSectorWithPlantsById(
                (GetSectorRequest.newBuilder().setId(resultSet.getLong("sector_plant_id")).build()
                )));
        }
        return orderBuilder.build();
    };

    @Override
    public Order createOrder(CreateOrderRequest order) {
        // Вставка заказа
        String insertOrderSql = """
            INSERT INTO "Order" (company_name, status, commentary)
            VALUES (?, ?, ?)
            """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(insertOrderSql, new String[]{"id"});
            ps.setString(1, order.getCompanyName());
            ps.setString(2, order.getStatus());
            ps.setString(3, order.getCommentary());
            return ps;
        }, keyHolder);

        // Вставка связи с SectorPlant

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
            SELECT o.id, o.company_name, o.status, o.commentary, spho.sector_plant_id
            FROM "Order" o
            LEFT JOIN SectorPlantHasOrder spho ON o.id = spho.order_id
            WHERE o.id = ?
            """;
        try {
            return jdbcTemplate.queryForObject(sql, ORDER_ROW_MAPPER, request.getId());
        } catch (Exception e) {
            throw new RuntimeException("Order with id " + request.getId() + " not found", e);
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
            throw new RuntimeException("Order with id " + order.getId() + " not found");
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
            throw new RuntimeException("Order with id " + request.getId() + " not found");
        }
    }

    @Override
    public GetAllOrdersResponse getAllOrders() {
        String sql = """
            SELECT o.id, o.company_name, o.status, o.commentary, spho.sector_plant_id
            FROM "Order" o
            LEFT JOIN SectorPlantHasOrder spho ON o.id = spho.order_id
            """;
        List<Order> orders = jdbcTemplate.query(sql, ORDER_ROW_MAPPER);

        log.info(Order.getDescriptor().toString());
        return GetAllOrdersResponse.newBuilder().addAllOrders(orders).build();
    }
}
