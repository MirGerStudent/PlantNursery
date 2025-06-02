package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.*;

public interface IEventRepository {
    EventShort CreateEvent (CreateEventRequest createEventRequest);
    EventShort GetEventById (GetEventRequest getEventRequest);
    EventShort UpdateEvent (EventShort event);
    void DeleteEvent (DeleteEventRequest deleteEventRequest);
}
