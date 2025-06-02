package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.*;

public interface IPlantTypeRepository {
    PlantType CreatePlantType (CreatePlantTypeRequest createPlantTypeRequest);
    PlantType UpdatePlantType (PlantType plantType);
    void DeletePlantType (GetPlantTypeRequest getPlantTypeRequest);
    AllPlantTypesResponse GetAllPlantTypes ();
    PlantType GetPlantTypeById (GetPlantTypeRequest getPlantTypeRequest);
}
