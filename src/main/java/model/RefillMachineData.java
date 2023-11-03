package model;

import lombok.Data;

@Data
public class RefillMachineData {
    public String type;
    public double leasingCostPerWeek = -1;
    public double refillCapacityPerWeek = -1;
    public double staticCo2 = -1;
}
