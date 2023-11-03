import model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Scoring {

    public GameData calculateScore(String mapName, SubmitSolution solution, MapData mapEntity, GeneralData generalData) {
        GameData gameDataScore = new GameData();
        gameDataScore.setMapName(mapName);
        gameDataScore.setTeamId(UUID.randomUUID());
        gameDataScore.setLocations(new HashMap<>());
        gameDataScore.setGameScore(new Score());

        Map<String, StoreLocationScoring> locationListNoRefillStation = new HashMap<>();

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
                    throw new RuntimeException(String.format("You are not allowed to submit locations with no refill stations. Remove or alter location: %s", locationValue.getLocationName()));
                }
            } else {
                StoreLocationScoring scoring = new StoreLocationScoring();
                scoring.setLocationName( locationValue.getLocationName());
                scoring.setLocationType(locationValue.getLocationType());
                scoring.setLatitude(locationValue.getLatitude());
                scoring.setLongitude(locationValue.getLongitude());
                scoring.setSalesVolume(locationValue.getSalesVolume() * generalData.getRefillSalesFactor());

                locationListNoRefillStation.put(locationKey, scoring);
            }
        }

        if (gameDataScore.getLocations().isEmpty()) {
            throw new RuntimeException(String.format("No valid locations with refill stations were placed for map: %s", mapName));
        }

        gameDataScore.setLocations(distributeSales(gameDataScore.getLocations(), locationListNoRefillStation, generalData));

        for (Map.Entry<String, StoreLocationScoring> storeLocationScoringEntry : gameDataScore.getLocations().entrySet()) {
            StoreLocationScoring storeLocationScoring = storeLocationScoringEntry.getValue();
            storeLocationScoring.setSalesVolume(BigDecimal.valueOf(storeLocationScoring.getSalesVolume()).setScale(0, RoundingMode.HALF_UP).doubleValue());

            double sales = storeLocationScoring.getSalesVolume();
            if (storeLocationScoring.getSalesCapacity() < sales) {
                sales = storeLocationScoring.getSalesCapacity();
            }

            storeLocationScoring.setGramCo2Savings(sales * (generalData.getClassicUnitData().getCo2PerUnitInGrams() - generalData.getRefillUnitData().getCo2PerUnitInGrams()));
            gameDataScore.getGameScore().setKgCo2Savings(gameDataScore.getGameScore().getKgCo2Savings() + storeLocationScoring.getGramCo2Savings() / 1000);

            if (storeLocationScoring.getGramCo2Savings() > 0) {
                storeLocationScoring.setCo2Saving(true);
            }

            storeLocationScoring.setRevenue(sales * generalData.getRefillUnitData().getProfitPerUnit());
            gameDataScore.setTotalRevenue(gameDataScore.getTotalRevenue() + storeLocationScoring.getRevenue());

            storeLocationScoring.setEarnings(storeLocationScoring.getRevenue() - storeLocationScoring.getLeasingCost());
            if (storeLocationScoring.getEarnings() > 0) {
                storeLocationScoring.setProfitable(true);
            }

            gameDataScore.setTotalLeasingCost(gameDataScore.getTotalLeasingCost() + storeLocationScoring.getLeasingCost());
            gameDataScore.setTotalFreestyle3100Count(gameDataScore.getTotalFreestyle3100Count() + storeLocationScoring.getFreestyle3100Count());
            gameDataScore.setTotalFreestyle9100Count(gameDataScore.getTotalFreestyle9100Count() + storeLocationScoring.getFreestyle9100Count());
            gameDataScore.getGameScore().setTotalFootfall(gameDataScore.getGameScore().getTotalFootfall() + storeLocationScoring.getFootfall());
        }

        gameDataScore.setTotalRevenue(BigDecimal.valueOf(gameDataScore.getTotalRevenue()).setScale(0, RoundingMode.HALF_UP).doubleValue());

        double totalCo2Savings = gameDataScore.getGameScore().getKgCo2Savings()
                - gameDataScore.getTotalFreestyle3100Count() * generalData.getFreestyle3100Data().getStaticCo2() / 1000
                - gameDataScore.getTotalFreestyle9100Count() * generalData.getFreestyle9100Data().getStaticCo2() / 1000;

        gameDataScore.getGameScore().setKgCo2Savings(BigDecimal.valueOf(totalCo2Savings).setScale(0, RoundingMode.HALF_UP).doubleValue());
        gameDataScore.getGameScore().setEarnings(gameDataScore.getTotalRevenue() - gameDataScore.getTotalLeasingCost());

        double totalScore = (gameDataScore.getGameScore().getKgCo2Savings() * generalData.getCo2PricePerKiloInSek()
                + gameDataScore.getGameScore().getEarnings()) * (1 + gameDataScore.getGameScore().getTotalFootfall());

        gameDataScore.getGameScore().setTotal(BigDecimal.valueOf(totalScore).setScale(0, RoundingMode.HALF_UP).doubleValue());

        return gameDataScore;
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
