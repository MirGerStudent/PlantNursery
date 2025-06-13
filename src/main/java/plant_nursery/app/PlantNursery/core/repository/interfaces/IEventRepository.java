package plant_nursery.app.PlantNursery.core.repository.interfaces;

import plant_nursery.app.PlantNursery.exception.RepositoryException;
import protobuf.*;

public interface IEventRepository {
    EventShort CreateEvent (CreateEventRequest createEventRequest) throws RepositoryException;
    EventShort GetEventById (GetEventRequest getEventRequest);
    EventShort UpdateEvent (EventShort event);
    void DeleteEvent (DeleteEventRequest deleteEventRequest);
}
