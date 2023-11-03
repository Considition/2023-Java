package model;

import lombok.Data;

import java.util.Map;

@Data
public class SubmitSolution {
    public Map<String, PlacedLocations> locations;
}
