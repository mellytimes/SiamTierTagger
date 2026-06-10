package work.siamnet.siamtier;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Fetches the SiamTier leaderboard and caches it. The API is a paginated board
 * keyed by username, but we index by UUID (robust to name changes) and keep a
 * by-name index for the /tier command. Caches are swapped atomically via
 * volatile references, so reads from the main thread are always consistent.
 */
public final class TierCache {

    private static final String API = "https://siamtier.siam-net.work/api/top";

    private final HttpClient http = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final Logger log;

    private volatile Map<String, PlayerTiers> byUuid = Map.of(); // key: dashless lowercase uuid
    private volatile Map<String, PlayerTiers> byName = Map.of(); // key: lowercase name

    public TierCache(Logger log) {
        this.log = log;
    }

    public PlayerTiers get(UUID uuid) {
        return byUuid.get(uuid.toString().replace("-", "").toLowerCase(Locale.ROOT));
    }

    public PlayerTiers getByName(String name) {
        return byName.get(name.toLowerCase(Locale.ROOT));
    }

    /** Blocking — fetches every page. Must be called off the main thread. */
    public void refresh() {
        Map<String, PlayerTiers> uuidMap = new HashMap<>();
        Map<String, PlayerTiers> nameMap = new HashMap<>();
        try {
            int page = 1, totalPages = 1;
            do {
                JsonObject root = getPage(page);
                if (root == null) break;
                totalPages = root.get("totalPages").getAsInt();
                JsonObject players = root.getAsJsonObject("players");
                for (var entry : players.entrySet()) {
                    PlayerTiers pt = parse(entry.getKey(), entry.getValue().getAsJsonObject());
                    uuidMap.put(pt.uuid(), pt);
                    nameMap.put(pt.name().toLowerCase(Locale.ROOT), pt);
                }
            } while (++page <= totalPages);
        } catch (Exception ex) {
            log.warning("[SiamTier] refresh failed, keeping previous cache: " + ex.getMessage());
            return;
        }
        byUuid = uuidMap;
        byName = nameMap;
        log.info("[SiamTier] cached " + uuidMap.size() + " ranked players");
    }

    private JsonObject getPage(int page) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API + "?page=" + page))
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            log.warning("[SiamTier] API page " + page + " returned HTTP " + res.statusCode());
            return null;
        }
        return gson.fromJson(res.body(), JsonObject.class);
    }

    private PlayerTiers parse(String name, JsonObject pl) {
        Map<String, String> ranks = new HashMap<>();
        JsonObject ranksObj = pl.getAsJsonObject("ranks");
        for (var entry : ranksObj.entrySet()) {
            JsonObject rank = entry.getValue().getAsJsonObject();
            if (rank.get("retired").getAsBoolean()) continue; // only show active tiers
            ranks.put(entry.getKey(), rank.get("rank").getAsString());
        }
        return new PlayerTiers(
                name,
                pl.get("uuid").getAsString().toLowerCase(Locale.ROOT),
                pl.get("points").getAsInt(),
                pl.get("overallRank").getAsInt(),
                ranks);
    }
}
