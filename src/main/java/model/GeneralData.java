package model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GeneralData {
    private ContainerData classicUnitData;
    private ContainerData refillUnitData;
    private RefillMachineData freestyle9100Data;
    private RefillMachineData freestyle3100Data;
    private Map<String, LocationType> locationTypes;
    private List<String> competitionMapNames;
    private List<String> trainingMapNames;
    private double co2PricePerKiloInSek = -1;
    private double willingnessToTravelInMeters = -1;
    private double constantExpDistributionFunction = -1;
    private double refillSalesFactor = -1;
    private double refillDistributionRate = -1;
}
