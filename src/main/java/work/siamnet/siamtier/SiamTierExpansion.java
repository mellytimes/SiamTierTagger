package work.siamnet.siamtier;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

/**
 * PlaceholderAPI expansion. Registered only if PlaceholderAPI is installed.
 *
 * Placeholders (all read from the cached leaderboard, empty if the player is
 * unranked):
 *   %siamtier_tier%          displayed tier, e.g. HT1
 *   %siamtier_mode%          gamemode of the displayed tier, e.g. SWORD
 *   %siamtier_icon%          gamemode icon glyph (needs the pack to render)
 *   %siamtier_points%        total points
 *   %siamtier_overall%       overall leaderboard rank
 *   %siamtier_tier_<mode>%   tier in a specific mode, e.g. %siamtier_tier_sword%
 */
public final class SiamTierExpansion extends PlaceholderExpansion {

    private final TierCache cache;
    private final String mode; // configured gamemode, or null for "best"

    public SiamTierExpansion(TierCache cache, String mode) {
        this.cache = cache;
        this.mode = mode;
    }

    @Override public @NotNull String getIdentifier() { return "siamtier"; }
    @Override public @NotNull String getAuthor() { return "Melly"; }
    @Override public @NotNull String getVersion() { return "1.0.0"; }
    @Override public boolean persist() { return true; } // stay registered across PAPI reloads

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";
        PlayerTiers pt = cache.get(player.getUniqueId());
        if (pt == null) return ""; // unranked

        String p = params.toLowerCase(Locale.ROOT);

        // %siamtier_tier_<mode>%  (but not the literal "tier_suffix" placeholder)
        if (p.startsWith("tier_") && !p.equals("tier_suffix")) {
            String m = p.substring(5).toUpperCase(Locale.ROOT);
            String rank = pt.ranks().get(m);
            return rank == null ? "" : rank;
        }

        return switch (p) {
            case "points"  -> String.valueOf(pt.points());
            case "overall" -> String.valueOf(pt.overallRank());
            case "tier" -> { Display d = display(pt); yield d == null ? "" : d.rank(); }
            case "mode" -> { Display d = display(pt); yield d == null ? "" : d.mode(); }
            case "icon" -> { Display d = display(pt); yield d == null ? "" : TierFormatter.iconChar(d.mode()); }
            // For LuckPerms prefix/suffix. Legacy color codes so colors survive into chat/tab.
            case "overall_prefix" -> {
                Display d = display(pt);
                yield d == null ? "" : "\u00A77#" + pt.overallRank() + "\u00A78 | ";
            }
            case "tier_suffix" -> {
                Display d = display(pt);
                yield d == null ? "" : "\u00A78 | " + TierFormatter.iconChar(d.mode()) + " "
                        + TierFormatter.legacyColor(d.rank()) + d.rank();
            }
            default -> null; // unknown placeholder
        };
    }

    private record Display(String mode, String rank) {}

    /** Same selection logic as the nametag: configured mode, else best tier. */
    private Display display(PlayerTiers pt) {
        if (pt.ranks().isEmpty()) return null;
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
}
