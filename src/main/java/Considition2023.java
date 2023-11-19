import model.*;
import model.types.MapNames;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Considition2023 {

    public static void main(String[] args) {
        String apiKey = "";

        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.out.println("Configure apiKey in main.");
            return;
        }

        System.out.println("1: " + MapNames.Stockholm);
        System.out.println("2: " + MapNames.Goteborg);
        System.out.println("3: " + MapNames.Malmo);
        System.out.println("4: " + MapNames.Uppsala);
        System.out.println("5: " + MapNames.Vasteras);
        System.out.println("6: " + MapNames.Orebro);
        System.out.println("7: " + MapNames.London);
        System.out.println("8: " + MapNames.Linkoping);
        System.out.println("9: " + MapNames.Berlin);
        System.out.println("10: " + "G-Sandbox");
        System.out.println("11: " + "S-Sandbox");

        System.out.print("Select the map you wish to play: ");
        Scanner scanner = new Scanner(System.in);
        String option = scanner.nextLine();

        String mapName = switch (option) {
            case "1" -> MapNames.Stockholm.toString();
            case "2" -> MapNames.Goteborg.toString();
            case "3" -> MapNames.Malmo.toString();
            case "4" -> MapNames.Uppsala.toString();
            case "5" -> MapNames.Vasteras.toString();
            case "6" -> MapNames.Orebro.toString();
            case "7" -> MapNames.London.toString();
            case "8" -> MapNames.Linkoping.toString();
            case "9" -> MapNames.Berlin.toString();
            case "10" -> "G-Sandbox";
            case "11" -> "S-Sandbox";
            default -> null;
        };

        if (mapName == null) {
            System.out.println("Invalid map selected");
            return;
        }

        GameProgram gameProgram = new GameProgram();
        MapData mapData = gameProgram.getMapData(mapName, apiKey);
        GeneralData generalData = gameProgram.getGeneralData();
        SubmitSolution solution = new SubmitSolution();
        solution.setLocations(new HashMap<>());
        boolean isHardcore = Scoring.sandBoxMaps.contains(mapName.toLowerCase());
        if (isHardcore) {
            if (mapData.getHotspots() != null && !mapData.getHotspots().isEmpty()) {
                Hotspot hotspot1 = mapData.getHotspots().get(0);
                Hotspot hotspot2 = mapData.getHotspots().get(1);
                PlacedLocations placedLocations = new PlacedLocations();
                placedLocations.setFreestyle3100Count(0);
                placedLocations.setFreestyle9100Count(1);
                LocationType grocerystorelarge = generalData.getLocationTypes().get("grocerystorelarge");
                placedLocations.setLocationType(grocerystorelarge == null ? "" : grocerystorelarge.getType());
                placedLocations.setLongitud(hotspot1.longitude);
                placedLocations.setLatitud(hotspot1.latitude);
                solution.locations.put("location1", placedLocations);
                PlacedLocations placedLocations2 = new PlacedLocations();
                placedLocations2.setFreestyle3100Count(0);
                placedLocations2.setFreestyle9100Count(1);
                LocationType groceryStore = generalData.getLocationTypes().get("groceryStore");
                placedLocations2.setLocationType(groceryStore == null ? "" : groceryStore.getType());
                placedLocations2.setLongitud(hotspot2.longitude);
                placedLocations2.setLatitud(hotspot2.latitude);
                solution.locations.put("location2", placedLocations2);
            }
        } else {
        for (Map.Entry<String, StoreLocation> locationKeyPair : mapData.getLocations().entrySet()) {
            StoreLocation location = locationKeyPair.getValue();
            double salesVolume = location.getSalesVolume();
            if (salesVolume > 100) {
                PlacedLocations placedLocations = new PlacedLocations();
                placedLocations.setFreestyle3100Count(0);
                placedLocations.setFreestyle9100Count(1);

                solution.getLocations().put(location.getLocationName(), placedLocations);
            }
        }
    }

        Scoring scoring = new Scoring();
        if (isHardcore) {
            String hardcoreValidation = scoring.sandboxValidation(mapName, solution, mapData);
            if (hardcoreValidation != null) {
                throw new Error("Hardcore validation failed " + hardcoreValidation);
            }
        }
        GameData score = new Scoring().calculateScore(mapName, solution, mapData, generalData);
        System.out.println(
                "Game score: " + BigDecimal.valueOf(score.getGameScore().getTotal()).setScale(0, RoundingMode.HALF_UP));

        GameData submit = gameProgram.submit(mapName, solution, apiKey);
        System.out.println("Solution succefully submitted to Considtion 2023.");
        System.out.println("Game Id: " + submit.id);
        System.out.println("Score " + submit.gameScore.total);

        scanner.close();
    }
}
