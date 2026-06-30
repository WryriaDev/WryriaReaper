package dev.wyrria.reaper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Tab completer for /reaper command.
 * Filters suggestions based on sender permissions.
 * Uses pre-allocated immutable lists — zero object allocation per tab.
 */
public final class ReaperTabCompleter implements TabCompleter {

    /** Pre-cached subcommand lists — no allocation on each tab event. */
    private static final List<String> ADMIN_SUBS = List.of("clear", "reload", "time");
    private static final List<String> USER_SUBS  = List.of("time");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                 @NotNull Command command,
                                                 @NotNull String alias,
                                                 @NotNull String[] args) {
        // Only complete the first argument
        if (args.length != 1) {
            return Collections.emptyList();
        }

        // Select available subcommands based on permission
        final List<String> available = sender.hasPermission("reaper.admin") ? ADMIN_SUBS : USER_SUBS;
        final String input = args[0].toLowerCase();

        // Filter by input prefix
        return available.stream()
                .filter(sub -> sub.startsWith(input))
                .toList();
    }
}
