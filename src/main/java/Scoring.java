import model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Scoring {
    public static List<String> sandBoxMaps = List.of("s-sandbox", "g-sandbox");

    public GameData calculateScore(String mapName, SubmitSolution solution, MapData mapEntity, GeneralData generalData) {
        GameData gameDataScore = new GameData();
        gameDataScore.setMapName(mapName);
        gameDataScore.setTeamId(UUID.randomUUID());
        gameDataScore.setLocations(new HashMap<>());
        gameDataScore.setGameScore(new Score());

        Map<String, StoreLocationScoring> locationListNoRefillStation = new HashMap<>();
        if (!sandBoxMaps.contains(mapName)) {

            for (Map.Entry<String, StoreLocation> storeLocationEntry : mapEntity.getLocations().entrySet()) {
                String locationKey = storeLocationEntry.getKey();
                StoreLocation locationValue = storeLocationEntry.getValue();

                if (solution.getLocations().containsKey(locationKey)) {
                    StoreLocationScoring storeLocationScoring = new StoreLocationScoring();
                    storeLocationScoring.setLocationName(locationValue.getLocationName());
                    storeLocationScoring.setLocationType(locationValue.getLocationType());
                    storeLocationScoring.setLatitude(locationValue.getLatitude());
                    storeLocationScoring.setLongitude(locationValue.getLongitude());
                    storeLocationScoring.setFootfall(locationValue.getFootfall());
                    storeLocationScoring.setFreestyle3100Count(solution.getLocations().get(locationKey).getFreestyle3100Count());
                    storeLocationScoring.setFreestyle9100Count(solution.getLocations().get(locationKey).getFreestyle9100Count());

                    double salesVolume = locationValue.getSalesVolume() * generalData.getRefillSalesFactor();
                    storeLocationScoring.setSalesVolume(salesVolume);

                    double salesCapacity = storeLocationScoring.getFreestyle3100Count() * generalData.getFreestyle3100Data().getRefillCapacityPerWeek()
                            + storeLocationScoring.getFreestyle9100Count() * generalData.getFreestyle9100Data().getRefillCapacityPerWeek();
                    storeLocationScoring.setSalesCapacity(salesCapacity);

                    double leasingCost = storeLocationScoring.getFreestyle3100Count() * generalData.getFreestyle3100Data().getLeasingCostPerWeek()
                            + storeLocationScoring.getFreestyle9100Count() * generalData.getFreestyle9100Data().getLeasingCostPerWeek();
                    storeLocationScoring.setLeasingCost(leasingCost);

                    gameDataScore.getLocations().put(locationKey, storeLocationScoring);

                    if (storeLocationScoring.getSalesCapacity() <= 0) {
                        return null;
                    }
                } else {
                    StoreLocationScoring scoring = new StoreLocationScoring();
                    scoring.setLocationName(locationValue.getLocationName());
                    scoring.setLocationType(locationValue.getLocationType());
                    scoring.setLatitude(locationValue.getLatitude());
                    scoring.setLongitude(locationValue.getLongitude());
                    scoring.setSalesVolume(locationValue.getSalesVolume() * generalData.getRefillSalesFactor());

                    locationListNoRefillStation.put(locationKey, scoring);
                }
            }

            if (gameDataScore.getLocations().isEmpty()) {
                return null;
            }

            gameDataScore.setLocations(distributeSales(gameDataScore.getLocations(), locationListNoRefillStation, generalData));
        } else {
            gameDataScore.setLocations(initiateSandboxLocations(gameDataScore.getLocations(), generalData, solution));
            gameDataScore.setLocations(calcualteFootfall(gameDataScore.getLocations(), mapEntity));
        }

        gameDataScore.setLocations(divideFootfall(gameDataScore.getLocations(), generalData));

        for (Map.Entry<String, StoreLocationScoring> storeLocationScoringEntry : gameDataScore.getLocations().entrySet()) {
            StoreLocationScoring storeLocationScoring = storeLocationScoringEntry.getValue();
            storeLocationScoring.setSalesVolume(BigDecimal.valueOf(storeLocationScoring.getSalesVolume()).setScale(0, RoundingMode.HALF_UP).doubleValue());
            if (storeLocationScoring.getFootfall() <= 0 && sandBoxMaps.contains(mapName)) {
                storeLocationScoring.setSalesVolume(0);
            }
            double sales = storeLocationScoring.getSalesVolume();
            if (storeLocationScoring.getSalesCapacity() < sales) {
                sales = storeLocationScoring.getSalesCapacity();
            }

            double gramCo2Savings = sales * (generalData.getClassicUnitData().getCo2PerUnitInGrams() - generalData.getRefillUnitData().getCo2PerUnitInGrams())
                    - storeLocationScoring.getFreestyle3100Count() * generalData.getFreestyle3100Data().staticCo2
                    - storeLocationScoring.getFreestyle9100Count() * generalData.getFreestyle9100Data().staticCo2;
            storeLocationScoring.setGramCo2Savings(gramCo2Savings);

            gameDataScore.getGameScore().setKgCo2Savings(gameDataScore.getGameScore().getKgCo2Savings()
                    + storeLocationScoring.getGramCo2Savings() / 1000);

            if (storeLocationScoring.getGramCo2Savings() > 0) {
                storeLocationScoring.setCo2Saving(true);
            }

            double revenue = sales * generalData.getRefillUnitData().getProfitPerUnit();
            storeLocationScoring.setRevenue(revenue);
            gameDataScore.setTotalRevenue(gameDataScore.getTotalRevenue() + storeLocationScoring.getRevenue());

            storeLocationScoring.setEarnings(storeLocationScoring.getRevenue() - storeLocationScoring.getLeasingCost());
            if (storeLocationScoring.getEarnings() > 0) {
                storeLocationScoring.setProfitable(true);
            }

            gameDataScore.setTotalLeasingCost(gameDataScore.getTotalLeasingCost() + storeLocationScoring.getLeasingCost());
            gameDataScore.setTotalFreestyle3100Count(gameDataScore.getTotalFreestyle3100Count() + storeLocationScoring.getFreestyle3100Count());
            gameDataScore.setTotalFreestyle9100Count(gameDataScore.getTotalFreestyle9100Count() + storeLocationScoring.getFreestyle9100Count());
            gameDataScore.getGameScore().setTotalFootfall(gameDataScore.getGameScore().getTotalFootfall() + storeLocationScoring.getFootfall() / 1000);
        }

        //Just some rounding for nice whole numbers
        //scoredSolution.TotalRevenue = Math.Round(scoredSolution.TotalRevenue, 2);
        gameDataScore.setTotalRevenue(BigDecimal.valueOf(gameDataScore.getTotalRevenue()).setScale(2, RoundingMode.HALF_UP).doubleValue());

        //scoredSolution.GameScore.KgCo2Savings = Math.Round(scoredSolution.GameScore.KgCo2Savings, 2);
        gameDataScore.getGameScore().setKgCo2Savings(BigDecimal.valueOf(gameDataScore.getGameScore().getKgCo2Savings()).setScale(2, RoundingMode.HALF_UP).doubleValue());

        //scoredSolution.GameScore.TotalFootfall = Math.Round(scoredSolution.GameScore.TotalFootfall, 4);
        gameDataScore.getGameScore().setTotalFootfall(BigDecimal.valueOf(gameDataScore.getGameScore().getTotalFootfall()).setScale(4, RoundingMode.HALF_UP).doubleValue());

        //Calculate Earnings
        //scoredSolution.GameScore.Earnings = (scoredSolution.TotalRevenue - scoredSolution.TotalLeasingCost) / 1000;
        gameDataScore.getGameScore().setEarnings((gameDataScore.getTotalRevenue() - gameDataScore.getTotalLeasingCost()) / 1000);




        //gameDataScore.setTotalRevenue(BigDecimal.valueOf(gameDataScore.getTotalRevenue()).setScale(0, RoundingMode.HALF_UP).doubleValue());

        /*double totalCo2Savings = gameDataScore.getGameScore().getKgCo2Savings()
                - gameDataScore.getTotalFreestyle3100Count() * generalData.getFreestyle3100Data().getStaticCo2() / 1000
                - gameDataScore.getTotalFreestyle9100Count() * generalData.getFreestyle9100Data().getStaticCo2() / 1000;*/

        //gameDataScore.getGameScore().setKgCo2Savings(BigDecimal.valueOf(totalCo2Savings).setScale(0, RoundingMode.HALF_UP).doubleValue());

        //Calculate total score
        /*scoredSolution.GameScore.Total = Math.Round(
                (scoredSolution.GameScore.KgCo2Savings * generalData.Co2PricePerKiloInSek + scoredSolution.GameScore.Earnings) *
                        (1 + scoredSolution.GameScore.TotalFootfall),
                2
        );*/
        double totalScore = (gameDataScore.getGameScore().getKgCo2Savings() * generalData.getCo2PricePerKiloInSek()
                + gameDataScore.getGameScore().getEarnings()) * (1 + gameDataScore.getGameScore().getTotalFootfall());

        gameDataScore.getGameScore().setTotal(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP).doubleValue());

        return gameDataScore;
    }

    public Map<String, StoreLocationScoring> calcualteFootfall(Map<String, StoreLocationScoring> locations, MapData mapEntity)
    {
        double maxFootfall = 0;
        for (Map.Entry<String, StoreLocationScoring> kvpLoc : locations.entrySet())
        {
            for (Hotspot hotspot : mapEntity.getHotspots())
            {
                double distanceInMeters = distanceBetweenPoint(
                        hotspot.getLatitude(), hotspot.getLongitude(), kvpLoc.getValue().getLatitude(), kvpLoc.getValue().getLongitude()
                );
                double maxSpread = hotspot.getSpread();
                if (distanceInMeters <= maxSpread)
                {
                    double val = hotspot.getFootfall() * (1 - (distanceInMeters / maxSpread));
                    kvpLoc.getValue().setFootfall( kvpLoc.getValue().getFootfall() + val / 10);
                }
            }
            if (maxFootfall < kvpLoc.getValue().getFootfall())
            {
                maxFootfall = kvpLoc.getValue().getFootfall();
            }
        }
        if (maxFootfall > 0)
        {
            for (Map.Entry<String, StoreLocationScoring> kvpLoc : locations.entrySet())
            {
                if (kvpLoc.getValue().getFootfall() > 0)
                {
                    kvpLoc.getValue().setFootfallScale((int) (kvpLoc.getValue().getFootfall() / maxFootfall * 10));
                    if (kvpLoc.getValue().getFootfallScale() == 0)
                    {
                        kvpLoc.getValue().setFootfallScale(1);
                    }
                }
            }
        }
        return locations;
    }
    private double getSalesVolume(String locationType, GeneralData generalData)
    {
        for (Map.Entry<String, LocationType> kvpLoc : generalData.getLocationTypes().entrySet())
        {
            if (locationType == kvpLoc.getValue().getType())
            {
                return kvpLoc.getValue().SalesVolume;
            }
        }
        return 0;
    }

    public Map<String, StoreLocationScoring> initiateSandboxLocations(Map<String, StoreLocationScoring> locations, GeneralData generalData, SubmitSolution request)
    {
        for (Map.Entry<String, PlacedLocations> kvpLoc : request.getLocations().entrySet())
        {
            double sv = getSalesVolume(kvpLoc.getValue().getLocationType(), generalData);

            StoreLocationScoring scoredSolution = new StoreLocationScoring();
            scoredSolution.setLongitude(kvpLoc.getValue().getLongitud());
            scoredSolution.setLatitude(kvpLoc.getValue().getLatitud());
            scoredSolution.setFreestyle3100Count(kvpLoc.getValue().getFreestyle3100Count());
            scoredSolution.setFreestyle9100Count(kvpLoc.getValue().getFreestyle9100Count());
            scoredSolution.setLocationType(kvpLoc.getValue().getLocationType());
            scoredSolution.setSalesVolume(sv);
            double salesCapacity = request.locations.get(kvpLoc.getKey()).getFreestyle3100Count()
                    * generalData.getFreestyle3100Data().getRefillCapacityPerWeek()
                    + request.locations.get(kvpLoc.getKey()).getFreestyle9100Count() * generalData.getFreestyle3100Data().getRefillCapacityPerWeek();
            scoredSolution.setSalesCapacity(salesCapacity);
            double leasingCost = request.locations.get(kvpLoc.getKey()).getFreestyle3100Count()
                    * generalData.getFreestyle3100Data().getLeasingCostPerWeek()
                    + request.locations.get(kvpLoc.getKey()).getFreestyle9100Count()
                    * generalData.getFreestyle9100Data().getLeasingCostPerWeek();
            scoredSolution.setLeasingCost(leasingCost);
            locations.put(kvpLoc.getKey(), scoredSolution);
        }
        for (Map.Entry<String, StoreLocationScoring> kvpScope : locations.entrySet())
        {
            int count = 1;
            //Dictionary<string, double> distributeSalesTo = new();
            for (Map.Entry<String, StoreLocationScoring> kvpSurrounding : locations.entrySet())
            {
                if (kvpScope.getKey() != kvpSurrounding.getKey())
                {
                    int distance = distanceBetweenPoint(
                            kvpScope.getValue().getLatitude(), kvpScope.getValue().getLongitude(), kvpSurrounding.getValue().getLatitude(), kvpSurrounding.getValue().getLongitude()
                    );
                    if (distance < generalData.getWillingnessToTravelInMeters())
                    {
                        count++;
                    }
                }
            }

            kvpScope.getValue().setSalesVolume(kvpScope.getValue().getSalesVolume() / count);

        }
        return locations;
    }

    public Map<String, StoreLocationScoring> divideFootfall(Map<String, StoreLocationScoring> locations, GeneralData generalData)
    {
        for (Map.Entry<String, StoreLocationScoring> kvpScope : locations.entrySet())
        {
            int count = 1;
            for (Map.Entry<String, StoreLocationScoring> kvpSurrounding : locations.entrySet())
            {
                if (kvpScope.getKey() != kvpSurrounding.getKey())
                {
                    int distance = distanceBetweenPoint(
                            kvpScope.getValue().getLatitude(), kvpScope.getValue().getLongitude(), kvpSurrounding.getValue().getLatitude(), kvpSurrounding.getValue().getLongitude()
                    );
                    if (distance < generalData.getWillingnessToTravelInMeters())
                    {
                        count++;
                    }
                }
            }

            kvpScope.getValue().setFootfall(kvpScope.getValue().getFootfall() / count);

        }
        return locations;
    }

    public String sandboxValidation(String inMapName, SubmitSolution request, MapData mapData)
    {
        int countGroceryStoreLarge = 0;
        int countGroceryStore = 0;
        int countConvenience = 0;
        int countGasStation = 0;
        int countKiosk = 0;
        final int maxGroceryStoreLarge = 5;
        final int maxGroceryStore = 20;
        final int maxConvenience = 20;
        final int maxGasStation = 8;
        final int maxKiosk = 3;
        final int totalStores = maxGroceryStoreLarge + maxGroceryStore + maxConvenience + maxGasStation + maxKiosk;
        String numberErrorMsg = String.format("locationName needs to start with location and followed with a number larger than 0 and less than %s.", totalStores + 1);
        String mapName = inMapName.toLowerCase();
        for (Map.Entry<String, PlacedLocations> kvp : request.getLocations().entrySet())
        {
            //Validate location name
            if (!kvp.getKey().startsWith("location"))
            {
                return String.format("%s %s is not a valid name", numberErrorMsg, kvp.getKey());
            }
            String loc_num = kvp.getKey().substring(8);
            if (loc_num.trim().isEmpty()) {

                return String.format("%s Nothing followed location in the locationName", numberErrorMsg);
            }
            try {
                int n = Integer.parseInt(loc_num);
                if (n <= 0 || n > totalStores)
                {
                    return String.format("%s %s is not within the constraints", numberErrorMsg, n);
                }
            } catch (Exception e) {
                return String.format("%s %s is not a number", numberErrorMsg, loc_num);
            }

            //Validate long and lat
            if (mapData.getBorder().getLatitudeMin() > kvp.getValue().getLatitud() || mapData.getBorder().getLatitudeMax() < kvp.getValue().getLatitud())
            {
                return  String.format("Latitude is missing or out of bounds for location : %s", kvp.getKey());
            }
            if (mapData.getBorder().latitudeMin > kvp.getValue().getLongitud() || mapData.getBorder().getLongitudeMax() < kvp.getValue().getLongitud())
            {
                return String.format("Longitude is missing or out of bounds for location : %s", kvp.getKey());
            }
            //Validate locationType
            if (kvp.getValue().getLocationType().isEmpty())
            {
                return String.format("locationType is missing for location) : %s", kvp.getKey());
            }
            else if (kvp.getValue().getLocationType().equalsIgnoreCase("Grocery-store-large"))
            {
                countGroceryStoreLarge += 1;
            }
            else if (kvp.getValue().getLocationType().equalsIgnoreCase("Grocery-store"))
            {
                countGroceryStore += 1;
            }
            else if (kvp.getValue().getLocationType().equalsIgnoreCase("Convenience"))
            {
                countConvenience += 1;
            }
            else if (kvp.getValue().getLocationType().equalsIgnoreCase("Gas-station"))
            {
                countGasStation += 1;
            }
            else if (kvp.getValue().getLocationType().equalsIgnoreCase("Kiosk"))
            {
                countKiosk += 1;
            }
            else
            {
                return String.format("locationType --> %s not valid (check GetGeneralGameData for correct values) for location : %s", kvp.getValue().getLocationType(), kvp.getKey());
            }
            //Validate that max number of location is not exceeded
            if (countGroceryStoreLarge > maxGroceryStoreLarge || countGroceryStore > maxGroceryStore ||
                    countConvenience > maxConvenience || countGasStation > maxGasStation ||
                    countKiosk > maxKiosk)
            {
                return String.format("Number of allowed locations exceeded for locationType: %s", kvp.getValue().getLocationType());
            }
        }
        return null;
    }
    private Map<String, StoreLocationScoring> distributeSales(Map<String, StoreLocationScoring> with, Map<String, StoreLocationScoring> without, GeneralData generalData) {
        for (Map.Entry<String, StoreLocationScoring> kvpWithout : without.entrySet()) {
            Map<String, Double> distributeSalesTo = new HashMap<>();

            for (Map.Entry<String, StoreLocationScoring> kvpWith : with.entrySet()) {
                double distance = distanceBetweenPoint(
                        kvpWithout.getValue().getLatitude(),
                        kvpWithout.getValue().getLongitude(),
                        kvpWith.getValue().getLatitude(),
                        kvpWith.getValue().getLongitude()
                );
                if (distance < generalData.getWillingnessToTravelInMeters()) {
                    distributeSalesTo.put(kvpWith.getKey(), distance);
                }
            }

            double total = 0;
            if (!distributeSalesTo.isEmpty()) {
                for (Map.Entry<String, Double> kvp : distributeSalesTo.entrySet()) {
                    distributeSalesTo.put(kvp.getKey(), Math.pow(generalData.getConstantExpDistributionFunction(), generalData.getWillingnessToTravelInMeters() - kvp.getValue()) - 1);
                    total += distributeSalesTo.get(kvp.getKey());
                }

                for (Map.Entry<String, Double> kvp : distributeSalesTo.entrySet()) {
                    StoreLocationScoring storeLocationScoring = with.get(kvp.getKey());
                    storeLocationScoring.setSalesVolume(storeLocationScoring.getSalesVolume() + distributeSalesTo.get(kvp.getKey()) / total
                            * generalData.getRefillDistributionRate() * kvpWithout.getValue().getSalesVolume());  // locationSalesFrom
                }
            }
        }

        return with;
    }


    private int distanceBetweenPoint(double latitude1, double longitude1, double latitude2, double longitude2) {
        double r = 6371e3;
        double latRadian1 = Math.toRadians(latitude1);
        double latRadian2 = Math.toRadians(latitude2);


        double latDelta = Math.toRadians(latitude2 - latitude1);
        double longDelta = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2) +
                Math.cos(latRadian1) * Math.cos(latRadian2) *
                        Math.sin(longDelta / 2) * Math.sin(longDelta / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (int) Math.round(r * c);

    }


}
