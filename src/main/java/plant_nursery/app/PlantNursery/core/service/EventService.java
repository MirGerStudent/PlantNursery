package plant_nursery.app.PlantNursery.core.service;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.auth.AuthInterceptor;
import plant_nursery.app.PlantNursery.core.auth.Constant;
import plant_nursery.app.PlantNursery.core.auth.JwtUtil;
import plant_nursery.app.PlantNursery.core.repository.EventRepository;
import protobuf.*;

@GRpcService
public class EventService extends EventServiceGrpc.EventServiceImplBase {
    private static final Context.Key<String> ROLE_KEY = Context.key("role");

    @Autowired
    EventRepository eventRepository;

    @Override
    public void createEvent(CreateEventRequest request, StreamObserver<EventShort> responseObserver) {
        EventShort createdEvent = eventRepository.CreateEvent(request);
        responseObserver.onNext(createdEvent);
        responseObserver.onCompleted();
    }

    @Override
    public void getEventById(GetEventRequest request, StreamObserver<EventShort> responseObserver) {
        EventShort getEvent = eventRepository.GetEventById(request);
        responseObserver.onNext(getEvent);
        responseObserver.onCompleted();
    }

    @Override
    public void updateEvent(EventShort request, StreamObserver<EventShort> responseObserver) {
        EventShort updatedEvent = eventRepository.UpdateEvent(request);
        responseObserver.onNext(updatedEvent);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteEvent(DeleteEventRequest request, StreamObserver<Empty> responseObserver) {
//        if (Constant.adminCheck(ROLE_KEY, responseObserver)) {

        if (Constant.adminCheck(String.valueOf(AuthInterceptor.USER_ROLE), responseObserver)) {
            eventRepository.DeleteEvent(request);
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }
    }
}
