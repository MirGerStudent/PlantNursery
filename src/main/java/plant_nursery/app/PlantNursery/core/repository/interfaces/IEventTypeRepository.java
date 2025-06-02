package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.AllEventTypesResponse;
import protobuf.CreateEventTypeRequest;
import protobuf.GetEventTypeRequest;
import protobuf.EventType;

public interface IEventTypeRepository {
    EventType CreateEventType (CreateEventTypeRequest createEventTypeRequest);
    EventType UpdateEventType (EventType EventType);
    void DeleteEventType (GetEventTypeRequest getEventTypeRequest);
    AllEventTypesResponse GetAllEventTypes ();
    EventType GetEventTypeById (GetEventTypeRequest getEventTypeRequest);    
}
