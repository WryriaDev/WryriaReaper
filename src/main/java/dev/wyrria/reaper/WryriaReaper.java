package dev.wyrria.reaper;

import dev.wyrria.reaper.command.ReaperCommand;
import dev.wyrria.reaper.command.ReaperTabCompleter;
import dev.wyrria.reaper.config.ConfigManager;
import dev.wyrria.reaper.task.CleanerTask;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * WryriaReaper — High-performance, minimalist entity cleaner.
 * Main plugin class: manages lifecycle and initializes modules.
 */
public final class WryriaReaper extends JavaPlugin {

    private ConfigManager configManager;
    private CleanerTask cleanerTask;

    @Override
    public void onEnable() {
        // Initialize configuration manager and load both YAML files
        configManager = new ConfigManager(this);
        configManager.load();

        // Start the cleaner task (async countdown + sync purge)
        cleanerTask = new CleanerTask(this, configManager);
        cleanerTask.start();

        // Register command executor and tab completer
        final PluginCommand command = getCommand("reaper");
        if (command != null) {
            final ReaperCommand executor = new ReaperCommand(this, configManager, cleanerTask);
            command.setExecutor(executor);
            command.setTabCompleter(new ReaperTabCompleter());
        }

        getLogger().info("WryriaReaper enabled — purge interval: " + configManager.getInterval() + "s");
    }

    @Override
    public void onDisable() {
        if (cleanerTask != null) {
            cleanerTask.stop();
        }
        getLogger().info("WryriaReaper disabled.");
    }

    /**
     * Reloads both configuration files and restarts the cleaner task.
     * Called by /reaper reload command.
     */
    public void reload() {
        configManager.load();
        cleanerTask.stop();
        cleanerTask.start();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CleanerTask getCleanerTask() {
        return cleanerTask;
    }
}
