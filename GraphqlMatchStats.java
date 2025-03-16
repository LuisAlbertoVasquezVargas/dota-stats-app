import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

public class GraphqlMatchStats {
    public static void main(String[] args) throws Exception {
        // Print available game versions
        printGameVersions();
        
        // Print win rates for a specific match by delegating the process to a separate function
        printWinRatesForMatch(8215869335L);
    }

    public static void printWinRatesForMatch(long matchId) throws Exception {
        Map<Integer, int[]> heroStats = findStatsByMatch(matchId);
        Map<Integer, Double> winRates = calculateWinRates(heroStats);
        System.out.printf("%nWin Rate per Hero for match %d:%n", matchId);
        for (Map.Entry<Integer, Double> entry : winRates.entrySet()) {
            System.out.printf("Hero %d: %.2f%%%n", entry.getKey(), entry.getValue());
        }
    }

    public static Map<Integer, Double> calculateWinRates(Map<Integer, int[]> heroStats) {
        Map<Integer, Double> winRates = new HashMap<>();
        for (Map.Entry<Integer, int[]> entry : heroStats.entrySet()) {
            int heroId = entry.getKey();
            int wins = entry.getValue()[0];
            int totalMatches = entry.getValue()[1];
            double winRate = (double) wins / totalMatches * 100;
            winRates.put(heroId, winRate);
        }
        return winRates;
    }

    public static void printGameVersions() throws Exception {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("STRATZ_API_KEY");
        HttpClient client = HttpClient.newHttpClient();
        String endpoint = "https://api.stratz.com/graphql";
        ObjectMapper mapper = new ObjectMapper();
        String query = """
            query {
              constants {
                gameVersions {
                  id
                  name
                  asOfDateTime
                }
              }
            }
            """;
        JsonNode result = executeGraphQLQuery(client, endpoint, apiKey, query, mapper);
        JsonNode gameVersions = result.path("data").path("constants").path("gameVersions");
        System.out.println("Game Versions:");
        for (JsonNode version : gameVersions) {
            int id = version.path("id").asInt();
            String name = version.path("name").asText();
            long asOfDateTime = version.path("asOfDateTime").asLong();
            System.out.printf("ID: %d, Name: %s, asOfDateTime: %d%n", id, name, asOfDateTime);
        }
    }

    public static Map<Integer, int[]> findStatsByMatch(long matchId) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("STRATZ_API_KEY");
        HttpClient client = HttpClient.newHttpClient();
        String endpoint = "https://api.stratz.com/graphql";
        ObjectMapper mapper = new ObjectMapper();
        String query = buildMatchQuery(matchId);
        JsonNode result = executeGraphQLQuery(client, endpoint, apiKey, query, mapper);
        JsonNode matchData = result.path("data").path("match");
        boolean didRadiantWin = matchData.path("didRadiantWin").asBoolean();
        JsonNode players = matchData.path("players");
        Map<Integer, int[]> heroStats = new HashMap<>();
        for (JsonNode player : players) {
            int heroId = player.path("hero").path("id").asInt();
            boolean isRadiant = player.path("isRadiant").asBoolean();
            int win = (isRadiant == didRadiantWin) ? 1 : 0;
            int[] stats = heroStats.getOrDefault(heroId, new int[]{0, 0});
            stats[0] += win;
            stats[1] += 1;
            heroStats.put(heroId, stats);
        }
        return heroStats;
    }

    private static JsonNode executeGraphQLQuery(HttpClient client, String endpoint, String apiKey, String query, ObjectMapper mapper) throws Exception {
        String queryJson = mapper.writeValueAsString(Map.of("query", query));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .header("User-Agent", "STRATZ_API")
            .POST(HttpRequest.BodyPublishers.ofString(queryJson))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status Code: " + response.statusCode());
        return mapper.readTree(response.body());
    }

    private static String buildMatchQuery(long matchId) {
        return String.format("""
            query {
              match(id: %d) {
                didRadiantWin
                players {
                  hero {
                    id
                  }
                  isRadiant
                }
              }
            }
            """, matchId);
    }
}
