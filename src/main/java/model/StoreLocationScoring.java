package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StoreLocationScoring {
    private String locationName;
    private String locationType;
    private double latitude;
    private double longitude;
    private double footfall;
    private int footfallScale;
    private double salesVolume;
    private double salesCapacity;
    private double leasingCost;
    private double revenue;
    private double earnings;
    private int freestyle9100Count;
    private int freestyle3100Count;
    private double gramCo2Savings;
    @JsonProperty("isProfitable")
    private boolean isProfitable;
    @JsonProperty("isCo2Saving")
    private boolean isCo2Saving;

}
