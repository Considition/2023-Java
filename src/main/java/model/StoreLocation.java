package model;

import lombok.Data;

@Data
public class StoreLocation {
    public  String locationName;
    public  String locationType;
    public double latitude;
    public double longitude;
    public double footfall;
    public int footfallScale;
    public double salesVolume;
}
