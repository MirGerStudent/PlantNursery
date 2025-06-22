package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.*;

public interface ISectorRepository {
    Sector createSector(CreateSectorRequest sector);
    SectorWithPlants createSectorWithPlants(CreateSectorWithPlantsRequest createSectorWithPlantsRequest);
    SectorEventsResponse getSectorEvents(GetSectorRequest getSectorRequest);
    Sector getSectorById(GetSectorRequest getSectorRequest);
    SectorWithPlants getSectorWithPlantsById(GetSectorRequest getSectorRequest);
    Sectors GetChildElements(GetSectorRequest getSectorRequest);
    SumPlantsOnChildSectors GetSumAllChildSectorPlants (GetSectorRequest getSectorRequest);
    void addEventForSector(EventForSectorRequest eventForSectorRequest);
    Sector updateSector(Sector sector);
    SectorWithPlants updateSectorWithPlants(UpdateSectorWithPlantsRequest updateSectorWithPlantsRequest);
    void deleteSector(GetSectorRequest getSectorRequest);
    void deleteSectorWithPlants(GetSectorRequest getSectorRequest);
}
