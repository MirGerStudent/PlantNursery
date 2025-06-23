package plant_nursery.app.PlantNursery.core.repository.interfaces;

import protobuf.*;

public interface IPlantRepository {
    Plant save(CreatePlantRequest createPlantRequest);
    Plant getPlantById(GetPlantRequest getPlantRequest);
    Plants getAllPlants(Empty empty);
    Plant updatePlant(UpdatePlantRequest updatePlantRequest);
    Plant updatePlantCharacteristics(UpdatePlantCharacteristicsRequest updatePlantCharacteristicsRequest);
    Plant updatePlantCharacteristic(UpdatePlantCharacteristicRequest updatePlantCharacteristicRequest);
    void deletePant(DeletePlantRequest deletePlantRequest);
}