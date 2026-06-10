package work.siamnet.siamtier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Map;

/**
 * Builds the nametag prefix: [gamemode icon] [tier] |  e.g. "{sword} HT1 | ".
 *
 * The gamemode icons are injected into the DEFAULT font by SiamTier-Pack
 * (assets/minecraft/font/default.json), mapped to PUA codepoints U+E000..U+E009.
 * They are emitted WITHOUT a font tag on purpose: over-head player nametags
 * ignore custom fonts and only render the default font, so the icons must live
 * there to show above the head (chat/tab honor custom fonts, but nametags don't).
 */
public final class TierFormatter {

    /** Gamemode key -> glyph codepoint index. Order MUST match default.json. */
    private static final Map<String, Integer> MODE_IDX = Map.ofEntries(
            Map.entry("CRYSTALS", 0),
            Map.entry("UHC", 1),
            Map.entry("DIAPOT", 2),
            Map.entry("NETHPOT", 3),
            Map.entry("SMP", 4),
            Map.entry("SWORD", 5),
            Map.entry("AXE", 6),
            Map.entry("MACE", 7),
            Map.entry("CART", 8),
            Map.entry("DIASMP", 9)
    );

    private TierFormatter() {}

    /** Lower weight = better tier. HT1=1, LT1=2, HT2=3 ... HT5=9, LT5=10. */
    public static int weight(String rank) {
        int num = Integer.parseInt(rank.substring(2));
        return num * 2 - (rank.startsWith("HT") ? 1 : 0);
    }

    /** Color by tier number: 1 = gold (top) down to 5 = dark gray (bottom). */
    public static TextColor color(String rank) {
        int num = Integer.parseInt(rank.substring(2));
        return switch (num) {
            case 1 -> NamedTextColor.GOLD;
            case 2 -> NamedTextColor.YELLOW;
            case 3 -> NamedTextColor.GREEN;
            case 4 -> NamedTextColor.GRAY;
            default -> NamedTextColor.DARK_GRAY;
        };
    }

    /** Same tier colors as legacy section-sign codes, for plain-string placeholders. */
    public static String legacyColor(String rank) {
        int num = Integer.parseInt(rank.substring(2));
        return switch (num) {
            case 1 -> "\u00A76"; // gold
            case 2 -> "\u00A7e"; // yellow
            case 3 -> "\u00A7a"; // green
            case 4 -> "\u00A77"; // gray
            default -> "\u00A78"; // dark gray
        };
    }

    /** Big gamemode icon (U+E000+), used in the over-head nametag. */
    public static Component icon(String mode) {
        Integer idx = MODE_IDX.get(mode);
        if (idx == null) return Component.empty();
        String glyph = new String(Character.toChars(0xE000 + idx));
        return Component.text(glyph); // default font -> renders in over-head nametags too
    }

    /** Normal-size gamemode icon (U+E010+), used in the /tier chat list. */
    public static Component iconSmall(String mode) {
        Integer idx = MODE_IDX.get(mode);
        if (idx == null) return Component.empty();
        String glyph = new String(Character.toChars(0xE010 + idx));
        return Component.text(glyph);
    }

    /** Raw normal-size icon character (U+E010+), for use in placeholder strings. */
    public static String iconChar(String mode) {
        Integer idx = MODE_IDX.get(mode);
        if (idx == null) return "";
        return new String(Character.toChars(0xE010 + idx));
    }

    /** The colored tier label, e.g. a gold "HT1". */
    public static Component tierText(String rank) {
        return Component.text(rank, color(rank));
    }
}
