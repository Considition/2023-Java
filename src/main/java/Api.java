import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.GameData;
import model.GeneralData;
import model.MapData;
import model.SubmitSolution;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class Api {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl = "https://api.considition.com/";

    public Api(HttpClient httpClient) {
        this.httpClient = httpClient;
    }


    public CompletableFuture<MapData> getMapDataAsync(String mapName, String apiKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "api/game/getmapdata?mapName=" + mapName))
                .header("x-api-key", apiKey)
                .GET()
                .build();


        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP error: " + response.statusCode());
                    }
                    String responseText = response.body();
                    MapData mapData;
                    try {
                        mapData = objectMapper.readValue(responseText, MapData.class);
                    } catch (JsonProcessingException e) {
                        mapData = null;
                        e.printStackTrace();
                    }
                    return mapData;
                });
    }

    public CompletableFuture<GeneralData> getGeneralDataAsync() throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "api/game/getgeneralgamedata"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP error: " + response.statusCode());
                    }
                    String responseText = response.body();
                    GeneralData generalData;
                    try {
                        generalData = objectMapper.readValue(responseText, GeneralData.class);
                    } catch (JsonProcessingException e) {
                        generalData = null;
                        e.printStackTrace();
                    }
                    return generalData;
                });
    }

    public CompletableFuture<GameData> getGameAsync(UUID id) throws URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "api/game/getgamedata/" + id))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP error: " + response.statusCode());
                    }
                    String responseText = response.body();
                    GameData gameData;
                    try {
                        gameData = objectMapper.readValue(responseText, GameData.class);
                    } catch (JsonProcessingException e) {
                        gameData = null;
                        e.printStackTrace();
                    }
                    return gameData;
                });
    }

    public CompletableFuture<GameData> submitAsync(String mapName, SubmitSolution solution, String apiKey) throws JsonProcessingException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(baseUrl + "/api/Game/submitSolution?mapName=" + mapName))
                .header("x-api-key", apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(solution)))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("HTTP error: " + response.statusCode());
                    }
                    String responseText = response.body();
                    GameData gameData;
                    try {
                        gameData = objectMapper.readValue(responseText, GameData.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        throw new Error("There was an error submitting the solution try again. " + e.getMessage());
                    }
                    return gameData;
                });
    }
}
