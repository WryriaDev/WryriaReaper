package dev.wyrria.reaper.command;

import dev.wyrria.reaper.WryriaReaper;
import dev.wyrria.reaper.config.ConfigManager;
import dev.wyrria.reaper.task.CleanerTask;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Command executor for /reaper.
 * Subcommands: clear, reload, time
 */
public final class ReaperCommand implements CommandExecutor {

    private final WryriaReaper plugin;
    private final ConfigManager config;
    private final CleanerTask task;

    public ReaperCommand(WryriaReaper plugin, ConfigManager config, CleanerTask task) {
        this.plugin = plugin;
        this.config = config;
        this.task   = task;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        // No arguments — show usage
        if (args.length == 0) {
            sender.sendMessage(config.getUsage());
            return true;
        }

        switch (args[0].toLowerCase()) {

            // ── /reaper clear ───────────────────────
            case "clear" -> {
                if (!sender.hasPermission("reaper.admin")) {
                    sender.sendMessage(config.getNoPermission());
                    return true;
                }
                task.forceClear();
            }

            // ── /reaper reload ──────────────────────
            case "reload" -> {
                if (!sender.hasPermission("reaper.admin")) {
                    sender.sendMessage(config.getNoPermission());
                    return true;
                }
                plugin.reload();
                sender.sendMessage(config.getReloadSuccess());
            }

            // ── /reaper time ────────────────────────
            case "time" -> {
                if (!sender.hasPermission("reaper.use")) {
                    sender.sendMessage(config.getNoPermission());
                    return true;
                }
                sender.sendMessage(config.getTimeRemaining(task.getSecondsRemaining()));
            }

            // ── Unknown subcommand ──────────────────
            default -> sender.sendMessage(config.getUsage());
        }

        return true;
    }
}
