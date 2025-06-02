package plant_nursery.app.PlantNursery.core.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.repository.EventTypeRepository;
import protobuf.*;

@GRpcService
public class EventTypeService extends EventTypeServiceGrpc.EventTypeServiceImplBase {
    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Override
    public void createEventType(CreateEventTypeRequest request, StreamObserver<EventType> responseObserver) {
        EventType newEventType = eventTypeRepository.CreateEventType(request);
        responseObserver.onNext(newEventType);
        responseObserver.onCompleted();
    }

    @Override
    public void updateEventType(EventType request, StreamObserver<EventType> responseObserver) {
        EventType updatedEventType = eventTypeRepository.UpdateEventType(request);
        responseObserver.onNext(updatedEventType);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteEventType(GetEventTypeRequest request, StreamObserver<Empty> responseObserver) {
        eventTypeRepository.DeleteEventType(request);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllEventTypes(Empty request, StreamObserver<AllEventTypesResponse> responseObserver) {
        AllEventTypesResponse allEventTypesResponse = eventTypeRepository.GetAllEventTypes();
        responseObserver.onNext(allEventTypesResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getEventTypeById(GetEventTypeRequest request, StreamObserver<EventType> responseObserver) {
        EventType eventType = eventTypeRepository.GetEventTypeById(request);
        responseObserver.onNext(eventType);
        responseObserver.onCompleted();
    }
}
