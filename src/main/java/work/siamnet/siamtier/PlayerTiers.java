package work.siamnet.siamtier;

import java.util.Map;

/**
 * Immutable snapshot of one player's SiamTier data.
 *
 * @param name        in-game username (as the API last saw it)
 * @param uuid        dashless, lowercase UUID
 * @param points      total points
 * @param overallRank position on the overall leaderboard
 * @param ranks       gamemode -> rank string (e.g. "HT3"); retired ranks excluded
 */
public record PlayerTiers(
        String name,
        String uuid,
        int points,
        int overallRank,
        Map<String, String> ranks
) {}
