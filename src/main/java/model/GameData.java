package model;

import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Data
public class GameData {

    public UUID id;
    public String mapName = "";
    public Score gameScore;
    public String teamName = "";
    public UUID teamId;
    public int totalFreestyle9100Count = 0;
    public int totalFreestyle3100Count = 0;
    public double totalLeasingCost = 0.0;
    public double totalRevenue = 0.0;
    public Map<String, StoreLocationScoring> locations;
    public Date timestamp;
}
