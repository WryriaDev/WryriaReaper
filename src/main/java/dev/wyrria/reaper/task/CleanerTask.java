package dev.wyrria.reaper.task;

import dev.wyrria.reaper.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cleaner task — the core purge engine.
 *
 * Architecture:
 *   - Countdown runs on an ASYNC thread via BukkitRunnable (no tick blocking)
 *   - Entity removal is always scheduled on the MAIN thread (Bukkit API safety)
 *   - Only loaded chunks are scanned (no unnecessary world loading)
 *   - AtomicInteger ensures thread-safe countdown access
 */
public final class CleanerTask {

    private final JavaPlugin plugin;
    private final ConfigManager config;

    /** Thread-safe countdown counter (seconds until next purge). */
    private final AtomicInteger countdown = new AtomicInteger();

    /** Active task reference for cancellation. */
    private BukkitTask activeTask;

    public CleanerTask(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    /**
     * Starts the async countdown timer.
     * Ticks every second (20 game ticks), checks for warning thresholds,
     * and triggers a sync purge when countdown reaches zero.
     */
    public void start() {
        countdown.set(config.getInterval());

        activeTask = new BukkitRunnable() {
            @Override
            public void run() {
                final int remaining = countdown.decrementAndGet();

                // Check warning thresholds (only if enabled in config)
                if (remaining == 60 && config.isWarn60()) {
                    broadcastOnMainThread(60);
                } else if (remaining == 30 && config.isWarn30()) {
                    broadcastOnMainThread(30);
                } else if (remaining == 10 && config.isWarn10()) {
                    broadcastOnMainThread(10);
                }

                // Time's up — purge entities
                if (remaining <= 0) {
                    executePurgeOnMainThread(false);
                    countdown.set(config.getInterval());
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L); // 1-second intervals
    }

    /**
     * Stops the active countdown task.
     */
    public void stop() {
        if (activeTask != null && !activeTask.isCancelled()) {
            activeTask.cancel();
            activeTask = null;
        }
    }

    /**
     * Returns the number of seconds until the next automatic purge.
     */
    public int getSecondsRemaining() {
        return countdown.get();
    }

    /**
     * Immediately purges all target entities and resets the countdown.
     * Used by /reaper clear command.
     */
    public void forceClear() {
        executePurgeOnMainThread(true);
        countdown.set(config.getInterval());
    }

    // ════════════════════════════════════════════════════
    //  Internal helpers
    // ════════════════════════════════════════════════════

    /**
     * Broadcasts a warning message on the main thread.
     * Adventure Components must be sent from the main thread.
     */
    private void broadcastOnMainThread(int seconds) {
        Bukkit.getScheduler().runTask(plugin, () ->
                Bukkit.getServer().sendMessage(config.getWarning(seconds))
        );
    }

    /**
     * Schedules entity removal on the main thread.
     * Scans ONLY loaded chunks for performance — never forces chunk loading.
     *
     * @param manual if true, sends the "force-clear" message instead of "clear-complete"
     */
    private void executePurgeOnMainThread(boolean manual) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            final Set<EntityType> targetTypes = config.getEntityTypes();
            final List<String> targetWorlds = config.getWorlds();
            int totalRemoved = 0;

            for (final World world : Bukkit.getWorlds()) {
                // Skip worlds not in the whitelist (if whitelist is non-empty)
                if (!targetWorlds.isEmpty() && !targetWorlds.contains(world.getName())) {
                    continue;
                }

                // Iterate only over loaded chunks — performance optimization
                for (final Chunk chunk : world.getLoadedChunks()) {
                    for (final Entity entity : chunk.getEntities()) {
                        if (targetTypes.contains(entity.getType())) {
                            if (entity instanceof org.bukkit.entity.Item item) {
                                totalRemoved += item.getItemStack().getAmount();
                            } else {
                                totalRemoved++;
                            }
                            entity.remove();
                        }
                    }
                }
            }

            // Broadcast result message with entity count
            if (manual) {
                Bukkit.getServer().sendMessage(config.getForceClear(totalRemoved));
            } else {
                Bukkit.getServer().sendMessage(config.getClearComplete(totalRemoved));
            }
        });
    }
}
