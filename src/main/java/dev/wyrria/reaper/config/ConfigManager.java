package dev.wyrria.reaper.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration manager for WryriaReaper.
 * Loads and caches values from config.yml and messages.yml,
 * and produces Adventure Components via MiniMessage.
 *
 * Supports both MiniMessage native {@code <#FF5555>} and
 * legacy {@code &#FF5555} hex color formats.
 */
public final class ConfigManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    /**
     * Pattern to match legacy hex color codes: {@code &#RRGGBB}
     * Converts them to MiniMessage-compatible {@code <#RRGGBB>} format.
     */
    private static final Pattern LEGACY_HEX = Pattern.compile("&#([0-9a-fA-F]{6})");

    // ── config.yml cached values ────────────────────────
    private int interval;
    private final Set<EntityType> entityTypes = EnumSet.noneOf(EntityType.class);
    private List<String> worlds = Collections.emptyList();
    private boolean warn60;
    private boolean warn30;
    private boolean warn10;

    // ── messages.yml cached values ──────────────────────
    private String prefix;
    private String warning60;
    private String warning30;
    private String warning10;
    private String clearComplete;
    private String forceClear;
    private String timeRemaining;
    private String reloadSuccess;
    private String noPermission;
    private String usage;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads (or reloads) both configuration files.
     * Called on enable and on /reaper reload.
     */
    public void load() {
        loadConfig();
        loadMessages();
    }

    // ════════════════════════════════════════════════════
    //  config.yml
    // ════════════════════════════════════════════════════

    private void loadConfig() {
        plugin.saveResource("config.yml", false);

        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "config.yml"));

        // Purge interval — enforce a minimum of 10 seconds
        interval = Math.max(10, cfg.getInt("interval", 300));

        // Parse entity types from string list
        entityTypes.clear();
        for (final String name : cfg.getStringList("entity-types")) {
            try {
                entityTypes.add(EntityType.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in config: " + name);
            }
        }

        // World whitelist (empty = all worlds)
        worlds = cfg.getStringList("worlds");

        // Warning toggles
        warn60 = cfg.getBoolean("warnings.60-seconds", true);
        warn30 = cfg.getBoolean("warnings.30-seconds", true);
        warn10 = cfg.getBoolean("warnings.10-seconds", true);
    }

    // ════════════════════════════════════════════════════
    //  messages.yml
    // ════════════════════════════════════════════════════

    private void loadMessages() {
        plugin.saveResource("messages.yml", false);

        final YamlConfiguration msg = YamlConfiguration.loadConfiguration(
                new File(plugin.getDataFolder(), "messages.yml"));

        prefix        = msg.getString("prefix", "<dark_gray>[<#b24bff>WryriaReaper</#b24bff>]");
        warning60     = msg.getString("warning-60", "<gray>Purge in {time}s.");
        warning30     = msg.getString("warning-30", "<gray>Purge in {time}s!");
        warning10     = msg.getString("warning-10", "<red>Purge in {time}s!");
        clearComplete = msg.getString("clear-complete", "<gray>Purge complete! {count} removed.");
        forceClear    = msg.getString("force-clear", "<gray>Manual purge! {count} removed.");
        timeRemaining = msg.getString("time-remaining", "<gray>Next purge in: {minutes}");
        reloadSuccess = msg.getString("reload-success", "<green>Config reloaded.");
        noPermission  = msg.getString("no-permission", "<red>No permission.");
        usage         = msg.getString("usage", "<gray>Usage: /reaper <clear|reload|time>");
    }

    // ════════════════════════════════════════════════════
    //  MiniMessage component builders
    // ════════════════════════════════════════════════════

    /**
     * Converts legacy {@code &#RRGGBB} hex codes to MiniMessage
     * {@code <#RRGGBB>} format, then deserializes with prefix.
     */
    private Component format(String raw) {
        final String converted = convertLegacyHex(prefix + " " + raw);
        return miniMessage.deserialize(converted);
    }

    /**
     * Replaces all {@code &#RRGGBB} occurrences with {@code <#RRGGBB>}.
     * This allows users to use either format in messages.yml.
     */
    private static String convertLegacyHex(String input) {
        final Matcher matcher = LEGACY_HEX.matcher(input);
        // Use StringBuilder for efficient replacement
        final StringBuilder sb = new StringBuilder(input.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, "<#" + matcher.group(1) + ">");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /** Countdown warning message with {time} placeholder. */
    public Component getWarning(int seconds) {
        final String raw;
        if (seconds == 60)      raw = warning60;
        else if (seconds == 30) raw = warning30;
        else                    raw = warning10;

        return format(raw.replace("{time}", String.valueOf(seconds)));
    }

    /** Automatic purge complete message with {count} placeholder. */
    public Component getClearComplete(int count) {
        return format(clearComplete.replace("{count}", String.valueOf(count)));
    }

    /** Manual purge complete message with {count} placeholder. */
    public Component getForceClear(int count) {
        return format(forceClear.replace("{count}", String.valueOf(count)));
    }

    /** Time remaining message with {minutes} placeholder (mm:ss format). */
    public Component getTimeRemaining(int secondsLeft) {
        final int min = secondsLeft / 60;
        final int sec = secondsLeft % 60;
        final String formatted = String.format("%d:%02d", min, sec);
        return format(timeRemaining.replace("{minutes}", formatted));
    }

    /** Reload success message. */
    public Component getReloadSuccess() {
        return format(reloadSuccess);
    }

    /** Insufficient permissions message. */
    public Component getNoPermission() {
        return format(noPermission);
    }

    /** Command usage message. */
    public Component getUsage() {
        return format(usage);
    }

    // ════════════════════════════════════════════════════
    //  Getters
    // ════════════════════════════════════════════════════

    public int getInterval() {
        return interval;
    }

    public Set<EntityType> getEntityTypes() {
        return Collections.unmodifiableSet(entityTypes);
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public boolean isWarn60() {
        return warn60;
    }

    public boolean isWarn30() {
        return warn30;
    }

    public boolean isWarn10() {
        return warn10;
    }
}
