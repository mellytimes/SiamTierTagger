package work.siamnet.siamtier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;

/**
 * Applies "[gamemode icon] [tier] | name" to player nametags via scoreboard
 * teams. The displayed tier is either the configured gamemode (if the player
 * is ranked in it) or their best tier across all modes; the icon shown is the
 * icon for whichever gamemode that tier belongs to.
 */
public final class TierTagApplier {

    /** A tier to display, paired with the gamemode it belongs to. */
    public record Display(String mode, String rank) {}

    private final TierCache cache;
    private final String mode; // configured gamemode, or null for "best"

    public TierTagApplier(TierCache cache, String mode) {
        this.cache = cache;
        this.mode = mode;
    }

    /** What to display for this player, or null if unranked. */
    public Display displayFor(Player p) {
        PlayerTiers pt = cache.get(p.getUniqueId());
        if (pt == null || pt.ranks().isEmpty()) return null;

        if (mode != null && pt.ranks().containsKey(mode)) {
            return new Display(mode, pt.ranks().get(mode));
        }
        // best tier across all modes
        String bestMode = null, bestRank = null;
        int bestWeight = Integer.MAX_VALUE;
        for (Map.Entry<String, String> e : pt.ranks().entrySet()) {
            int w = TierFormatter.weight(e.getValue());
            if (w < bestWeight) {
                bestWeight = w;
                bestMode = e.getKey();
                bestRank = e.getValue();
            }
        }
        return new Display(bestMode, bestRank);
    }

    public void apply(Player p) {
        Display d = displayFor(p);
        if (d == null) return;
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "st_" + d.mode() + "_" + d.rank(); // <= 16 chars for all modes
        Team team = sb.getTeam(teamName);
        if (team == null) {
            team = sb.registerNewTeam(teamName);
            team.prefix(TierFormatter.prefix(d.mode(), d.rank()));
        }
        team.addEntry(p.getName());
    }

    public void applyAll() {
        Bukkit.getOnlinePlayers().forEach(this::apply);
    }
}
