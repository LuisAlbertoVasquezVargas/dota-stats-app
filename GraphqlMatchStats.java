import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

public class GraphqlMatchStats {
    public static void main(String[] args) throws Exception {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("STRATZ_API_KEY");
        HttpClient client = HttpClient.newHttpClient();
        String graphqlEndpoint = "https://api.stratz.com/graphql";
        String query = """
            query {
              match(id: 8203479956) {
                didRadiantWin
                players {
                  hero {
                    id
                  }
                  isRadiant
                }
              }
            }
            """;
        ObjectMapper mapper = new ObjectMapper();
        String queryJson = mapper.writeValueAsString(Map.of("query", query));
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(graphqlEndpoint))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .header("User-Agent", "STRATZ_API")
            .POST(HttpRequest.BodyPublishers.ofString(queryJson))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status Code: " + response.statusCode());
        JsonNode root = mapper.readTree(response.body());
        JsonNode matchData = root.path("data").path("match");
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
        System.out.println("Win Rate per Hero:");
        for (Map.Entry<Integer, int[]> entry : heroStats.entrySet()) {
            int heroId = entry.getKey();
            int wins = entry.getValue()[0];
            int totalMatches = entry.getValue()[1];
            double winRate = (double) wins / totalMatches * 100;
            System.out.printf("Hero %d: %.2f%% (%d win(s) out of %d match(es))%n", heroId, winRate, wins, totalMatches);
        }
    }
}
