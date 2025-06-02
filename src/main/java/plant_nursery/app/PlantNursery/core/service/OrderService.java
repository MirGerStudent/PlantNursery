package plant_nursery.app.PlantNursery.core.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.repository.OrderRepository;
import protobuf.*;

@GRpcService
public class OrderService extends OrderServiceGrpc.OrderServiceImplBase {
    @Autowired
    OrderRepository orderRepository;

    @Override
    public void createOrder(CreateOrderRequest request, StreamObserver<Order> responseObserver) {
        Order order = orderRepository.createOrder(request);
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrderById(GetOrderRequest request, StreamObserver<Order> responseObserver) {
        Order order = orderRepository.getOrderById(request);
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void updateOrder(UpdateOrderRequest request, StreamObserver<Order> responseObserver) {
        Order order = orderRepository.updateOrder(request);
        responseObserver.onNext(order);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteOrder(GetOrderRequest request, StreamObserver<Empty> responseObserver) {
        orderRepository.deleteOrder(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAllOrders(Empty request, StreamObserver<GetAllOrdersResponse> responseObserver) {
        GetAllOrdersResponse allOrdersResponse = orderRepository.getAllOrders();
        responseObserver.onNext(allOrdersResponse);
        responseObserver.onCompleted();
    }
}
