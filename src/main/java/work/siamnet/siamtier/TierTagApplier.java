package work.siamnet.siamtier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Map;

/**
 * Applies the over-head tag in the format:  #<position> | <name> | <icon> <tier>
 *
 * The name sits in the middle, so we use the team PREFIX for "#pos | " and the
 * team SUFFIX for " | icon tier". Because the position number is unique to each
 * player, every player gets their own team (keyed by UUID) instead of a shared
 * per-tier team.
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

    /** Configured mode if ranked in it, else the best tier across all modes. */
    public Display displayFor(PlayerTiers pt) {
        if (pt == null || pt.ranks().isEmpty()) return null;
        if (mode != null && pt.ranks().containsKey(mode)) {
            return new Display(mode, pt.ranks().get(mode));
        }
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
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = p.getUniqueId().toString().replace("-", "").substring(0, 16);
        Team team = sb.getTeam(teamName);

        PlayerTiers pt = cache.get(p.getUniqueId());
        Display d = displayFor(pt);
        if (d == null) { // unranked: clear any tag they had
            if (team != null) team.removeEntry(p.getName());
            return;
        }
        if (team == null) team = sb.registerNewTeam(teamName);

        // #<position> |    (before the name)
        team.prefix(Component.text("#" + pt.overallRank(), NamedTextColor.GRAY)
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY)));

        // | <icon> <tier>    (after the name)
        team.suffix(Component.text(" | ", NamedTextColor.DARK_GRAY)
                .append(TierFormatter.icon(d.mode()))
                .append(Component.text(" "))
                .append(TierFormatter.tierText(d.rank())));

        team.addEntry(p.getName());
    }

    public void applyAll() {
        Bukkit.getOnlinePlayers().forEach(this::apply);
    }
}
