package work.siamnet.siamtier;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

/**
 * Entry point. Loads config, wires the pieces together, and schedules the
 * leaderboard refresh. Each responsibility lives in its own class:
 *   TierCache       - fetches + caches the API data
 *   TierFormatter   - colors + tier comparison
 *   TierTagApplier  - nametag prefixes via scoreboard teams
 *   JoinListener    - tags players on join
 *   TierCommand     - /tier [player] lookup
 */
public final class SiamTier extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        String raw = getConfig().getString("gamemode", "");
        String mode = (raw == null || raw.isBlank()) ? null : raw.trim().toUpperCase();
        long minutes = Math.max(1, getConfig().getLong("refresh-minutes", 5));

        TierCache cache = new TierCache(getLogger());
        TierTagApplier applier = new TierTagApplier(cache, mode);

        getServer().getPluginManager().registerEvents(new JoinListener(applier), this);
        getCommand("tier").setExecutor(new TierCommand(cache));

        // Refresh off the main thread, then re-apply tags back on the main thread.
        getServer().getAsyncScheduler().runAtFixedRate(this, task -> {
            cache.refresh();
            getServer().getScheduler().runTask(this, applier::applyAll);
        }, 1, minutes, TimeUnit.MINUTES);

        getLogger().info("SiamTierTagger enabled (mode=" + (mode == null ? "best" : mode)
                + ", refresh=" + minutes + "m)");
    }
}
