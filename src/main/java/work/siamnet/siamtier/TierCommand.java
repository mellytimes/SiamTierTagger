package work.siamnet.siamtier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * /tier            - show your own tiers
 * /tier <player>   - show another player's tiers
 *
 * Lists every active gamemode with its icon, name, and colored tier.
 */
public final class TierCommand implements CommandExecutor {

    private final TierCache cache;

    public TierCommand(TierCache cache) {
        this.cache = cache;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String name;
        if (args.length >= 1) {
            name = args[0];
        } else if (sender instanceof Player p) {
            name = p.getName();
        } else {
            sender.sendMessage(Component.text("Usage: /tier <player>", NamedTextColor.RED));
            return true;
        }

        PlayerTiers pt = cache.getByName(name);
        if (pt == null) {
            sender.sendMessage(Component.text(name + " is unranked on SiamTier.", NamedTextColor.GRAY));
            return true;
        }

        sender.sendMessage(Component.text("\u2014 " + pt.name() + " \u2014", NamedTextColor.AQUA)
                .append(Component.text("  #" + pt.overallRank() + " \u00b7 " + pt.points() + " pts",
                        NamedTextColor.DARK_GRAY)));

        if (pt.ranks().isEmpty()) {
            sender.sendMessage(Component.text("No active tiers.", NamedTextColor.GRAY));
            return true;
        }
        for (Map.Entry<String, String> e : pt.ranks().entrySet()) {
            sender.sendMessage(Component.text()
                    .append(TierFormatter.iconSmall(e.getKey()))
                    .append(Component.text(" " + e.getKey() + ": ", NamedTextColor.WHITE))
                    .append(TierFormatter.tierText(e.getValue()))
                    .build());
        }
        return true;
    }
}
