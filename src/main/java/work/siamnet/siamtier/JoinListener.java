package work.siamnet.siamtier;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/** Tags players with their tier the moment they join. */
public final class JoinListener implements Listener {

    private final TierTagApplier applier;

    public JoinListener(TierTagApplier applier) {
        this.applier = applier;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        applier.apply(event.getPlayer());
    }
}
