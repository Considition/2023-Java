package model;

import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Data
public class MapData {
    public String mapName;
    public  Border border;
    public HashMap<String, StoreLocation> locations;
    public List<Hotspot> hotspots;
    public HashMap<String, Integer> locationTypeCount;
    public Date availableFrom;
    public  Date availableTo;

}
