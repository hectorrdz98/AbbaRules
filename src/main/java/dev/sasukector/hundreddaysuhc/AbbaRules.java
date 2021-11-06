package dev.sasukector.hundreddaysuhc;

import dev.sasukector.hundreddaysuhc.commands.PointsCommand;
import dev.sasukector.hundreddaysuhc.commands.GameCommand;
import dev.sasukector.hundreddaysuhc.controllers.BoardController;
import dev.sasukector.hundreddaysuhc.events.SpawnEvents;
import dev.sasukector.hundreddaysuhc.helpers.ServerUtilities;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class AbbaRules extends JavaPlugin {

    private static @Getter AbbaRules instance;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(ChatColor.DARK_PURPLE + "AbbaRules startup!");
        instance = this;

        // Register events
        this.getServer().getPluginManager().registerEvents(new SpawnEvents(), this);
        Bukkit.getOnlinePlayers().forEach(player -> BoardController.getInstance().newPlayerBoard(player));

        // Register commands
        Objects.requireNonNull(AbbaRules.getInstance().getCommand("game")).setExecutor(new GameCommand());
        Objects.requireNonNull(AbbaRules.getInstance().getCommand("points")).setExecutor(new PointsCommand());

        // Set lobby spawn
        ServerUtilities.setLobbySpawn(new Location(ServerUtilities.getOverworld(), 0, 100, 0));
        World overworld = ServerUtilities.getOverworld();
        if (overworld != null) {
            overworld.getWorldBorder().setSize(4000);
            overworld.setGameRule(GameRule.KEEP_INVENTORY, true);
            overworld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            overworld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
            overworld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            overworld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(ChatColor.DARK_PURPLE + "AbbaRules shutdown!");
    }
}
