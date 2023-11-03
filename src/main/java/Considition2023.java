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

        GameData score = new Scoring().calculateScore(mapName, solution, mapData, generalData);
        System.out.println(
                "Game score: " + BigDecimal.valueOf(score.getGameScore().getTotal()).setScale(0, RoundingMode.HALF_UP));

        GameData submit = gameProgram.submit(mapName, solution, apiKey);
        System.out.println("Solution succefully submitted to Considtion 2023.");
        System.out.println("Game Id: " + submit.id);

        scanner.close();
    }
}
