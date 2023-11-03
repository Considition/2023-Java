package model;

import lombok.Data;

@Data
public class ContainerData {
    private String type;
    private double co2PerUnitInGrams = -1;
    private double profitPerUnit = -1;
}
