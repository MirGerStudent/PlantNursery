package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.*;

public interface IOrderRepository {
    Order createOrder (CreateOrderRequest order);
    Order getOrderById (GetOrderRequest getOrderRequest);
    Order updateOrder (UpdateOrderRequest order);
    void deleteOrder (GetOrderRequest getOrderRequest);
    GetAllOrdersResponse getAllOrders ();
}
