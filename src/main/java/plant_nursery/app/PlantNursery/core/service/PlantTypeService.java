package plant_nursery.app.PlantNursery.core.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.repository.PlantTypeRepository;
import protobuf.*;

@GRpcService
public class PlantTypeService extends PlantTypeServiceGrpc.PlantTypeServiceImplBase {
    @Autowired
    private PlantTypeRepository plantTypeRepository;

    @Override
    public void createPlantType(CreatePlantTypeRequest request, StreamObserver<PlantType> responseObserver) {
        PlantType newPlantType = plantTypeRepository.CreatePlantType(request);
        responseObserver.onNext(newPlantType);
        responseObserver.onCompleted();
    }

    @Override
    public void updatePlantType(PlantType request, StreamObserver<PlantType> responseObserver) {
        PlantType updatedPlantType = plantTypeRepository.UpdatePlantType(request);
        responseObserver.onNext(updatedPlantType);
        responseObserver.onCompleted();
    }

    @Override
    public void deletePlantType(GetPlantTypeRequest request, StreamObserver<Empty> responseObserver) {
        plantTypeRepository.DeletePlantType(request);
        Empty empty = Empty.newBuilder().build();
        responseObserver.onNext(empty);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllPlantTypes(Empty request, StreamObserver<AllPlantTypesResponse> responseObserver) {
        AllPlantTypesResponse allPlantTypesResponse = plantTypeRepository.GetAllPlantTypes();
        responseObserver.onNext(allPlantTypesResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getPlantTypeById(GetPlantTypeRequest request, StreamObserver<PlantType> responseObserver) {
        PlantType plantType = plantTypeRepository.GetPlantTypeById(request);
        responseObserver.onNext(plantType);
        responseObserver.onCompleted();
    }
}
