import model.GameData;
import model.GeneralData;
import model.MapData;
import model.SubmitSolution;

import java.net.http.HttpClient;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GameProgram {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public MapData getMapData(String mapName, String apiKey) {

        MapData mapData;
        try {
            Api api = new Api(httpClient);
            CompletableFuture<MapData> mapDataCompletableFuture = api.getMapDataAsync(mapName, apiKey);
            mapDataCompletableFuture.join();
             mapData = mapDataCompletableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Could not fetch data " + e.getMessage());

        }

        return mapData;
    }

    public GeneralData getGeneralData() {

        GeneralData mapData;
        try {
            Api api = new Api(httpClient);
            CompletableFuture<GeneralData> mapDataCompletableFuture =  api.getGeneralDataAsync();
            mapDataCompletableFuture.join();
            mapData = mapDataCompletableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Could not fech general data " + e.getMessage());

        }

        return mapData;
    }

    public GameData getGameData(UUID gameId) {
        GameData gameData;
        try {
            Api api = new Api(httpClient);
            CompletableFuture<GameData> mapDataCompletableFuture =  api.getGameAsync(gameId);
            mapDataCompletableFuture.join();
            gameData = mapDataCompletableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Could not fetch game data " + e.getMessage());

        }

        return gameData;
    }

    public GameData submit(String mapName, SubmitSolution solution, String apiKey) {

        GameData gameData;
        try {
            Api api = new Api(httpClient);
            CompletableFuture<GameData> mapDataCompletableFuture = api.submitAsync(mapName, solution, apiKey);
            mapDataCompletableFuture.join();
            gameData = mapDataCompletableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Could not submit data " + e.getMessage());
        }

        return gameData;
    }


}
