package model;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitSolution {
    public double longitude;
    public String locationType = "";
    public Map<String, PlacedLocations> locations;
}
