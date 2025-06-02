package plant_nursery.app.PlantNursery.core.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.repository.PlantRepository;
import protobuf.*;

@GRpcService
public class PlantService extends PlantServiceGrpc.PlantServiceImplBase {
    @Autowired
    private PlantRepository plantRepository;

    @Override
    public void createPlant(CreatePlantRequest request, StreamObserver<Plant> observer) {
        Plant newPlant = plantRepository.save(request);
        observer.onNext(newPlant);
        observer.onCompleted();
    }

    @Override
    public void updatePlant(UpdatePlantRequest request, StreamObserver<Plant> observer) {
        Plant updatePlant = plantRepository.updatePlant(request);
        observer.onNext(updatePlant);
        observer.onCompleted();
    }

    @Override
    public void getPlant(GetPlantRequest request, StreamObserver<Plant> responseObserver) {
        Plant getPlant = plantRepository.getPlantById(request);
        responseObserver.onNext(getPlant);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePlantCharacteristics(UpdatePlantCharacteristicsRequest request, StreamObserver<Plant> responseObserver) {
        Plant updatePlant = plantRepository.updatePlantCharacteristics(request);
        responseObserver.onNext(updatePlant);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePlantCharacteristic(UpdatePlantCharacteristicRequest request, StreamObserver<Plant> responseObserver) {
        Plant updatePlant = plantRepository.updatePlantCharacteristic(request);
        responseObserver.onNext(updatePlant);
        responseObserver.onCompleted();
    }

    @Override
    public void deletePlant(DeletePlantRequest request, StreamObserver<Empty> responseObserver) {
        plantRepository.deletePant(request);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }
}