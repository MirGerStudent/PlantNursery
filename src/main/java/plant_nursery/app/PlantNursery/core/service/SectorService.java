package plant_nursery.app.PlantNursery.core.service;

import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import plant_nursery.app.PlantNursery.core.repository.SectorRepository;
import protobuf.*;

@GRpcService
public class SectorService extends SectorServiceGrpc.SectorServiceImplBase {
    @Autowired
    private SectorRepository sectorRepository;

    @Override
    public void createSector(CreateSectorRequest request, StreamObserver<Sector> responseObserver) {
        Sector sector = sectorRepository.createSector(request);
        responseObserver.onNext(sector);
        responseObserver.onCompleted();
    }

    @Override
    public void createSectorWithPlants(CreateSectorWithPlantsRequest request, StreamObserver<SectorWithPlants> responseObserver) {
        SectorWithPlants sector = sectorRepository.createSectorWithPlants(request);
        responseObserver.onNext(sector);
        responseObserver.onCompleted();
    }

    @Override
    public void getSectorEvents(GetSectorRequest request, StreamObserver<SectorEventsResponse> responseObserver) {
        SectorEventsResponse response = sectorRepository.getSectorEvents(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getSector(GetSectorRequest request, StreamObserver<Sector> responseObserver) {
        Sector sector = sectorRepository.getSectorById(request);
        responseObserver.onNext(sector);
        responseObserver.onCompleted();
    }

    @Override
    public void getSectorWithPlants(GetSectorRequest request, StreamObserver<SectorWithPlants> responseObserver) {
        SectorWithPlants sector = sectorRepository.getSectorWithPlantsById(request);
        responseObserver.onNext(sector);
        responseObserver.onCompleted();
    }

    @Override
    public void getChildElements(GetSectorRequest request, StreamObserver<Sectors> responseObserver) {
        Sectors sectors = sectorRepository.GetChildElements(request);
        responseObserver.onNext(sectors);
        responseObserver.onCompleted();
    }

    @Override
    public void getSumAllChildSectorPlants(GetSectorRequest request, StreamObserver<SumPlantsOnChildSectors> responseObserver) {
        SumPlantsOnChildSectors sumPlantsOnChildSectors = sectorRepository.GetSumAllChildSectorPlants(request);
        responseObserver.onNext(sumPlantsOnChildSectors);
        responseObserver.onCompleted();
    }

    @Override
    public void addEventForSector(EventForSectorRequest request, StreamObserver<Empty> responseObserver) {
        sectorRepository.addEventForSector(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateSector(Sector request, StreamObserver<Sector> responseObserver) {
        Sector sector = sectorRepository.updateSector(request);
        responseObserver.onNext(sector);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSectorWithPlants(UpdateSectorWithPlantsRequest request, StreamObserver<SectorWithPlants> responseObserver) {
        SectorWithPlants sector = sectorRepository.updateSectorWithPlants(request);
        responseObserver.onNext(sector);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSector(GetSectorRequest request, StreamObserver<Empty> responseObserver) {
        sectorRepository.deleteSector(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteSectorWithPlants(GetSectorRequest request, StreamObserver<Empty> responseObserver) {
        sectorRepository.deleteSectorWithPlants(request);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }
}
