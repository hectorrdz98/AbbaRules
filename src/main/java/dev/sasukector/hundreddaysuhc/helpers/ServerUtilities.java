package dev.sasukector.hundreddaysuhc.helpers;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerUtilities {

    private final static @Getter MiniMessage miniMessage = MiniMessage.get();
    private static @Getter @Setter Location lobbySpawn = null;
    private static final Random random = new Random();

    // Associate all world names
    private final static Map<String, String> worldsNames;
    static {
        worldsNames = new HashMap<>();
        worldsNames.put("overworld", "world");
        worldsNames.put("nether", "world_nether");
        worldsNames.put("end", "world_the_end");
    }

    public static World getOverworld() {
        if (worldsNames.containsKey("overworld")) {
            return Bukkit.getWorld(worldsNames.get("overworld"));
        }
        return null;
    }

    public static Component getPluginNameColored() {
        return miniMessage.parse("<bold><gradient:#B5179E:#480CA8>Abba Rules</gradient></bold>");
    }

    public static void sendBroadcastMessage(Component message) {
        Bukkit.broadcast(getPluginNameColored()
                .append(Component.text(" ▶ ", TextColor.color(0xC0C1C2)))
                .append(message));
    }

    public static void sendServerMessage(Player player, String message) {
        player.sendMessage(getPluginNameColored()
                .append(Component.text(" ▶ ", TextColor.color(0xC0C1C2)))
                .append(Component.text(message, TextColor.color(0xFFFFFF))));
    }

    public static void sendServerMessage(Player player, Component message) {
        player.sendMessage(getPluginNameColored()
                .append(Component.text(" ▶ ", TextColor.color(0xC0C1C2)))
                .append(message));
    }

    private static Location getSafeLocation(Location location, List<Integer> ys) {
        Location newLocation = null;
        for (int y : ys) {
            location.setY(y);
            Block cBlock = location.getBlock();
            Block tBlock = location.add(0, 1, 0).getBlock();
            Block lBlock = location.add(0, -2, 0).getBlock();
            if (cBlock.getType() == Material.WATER && lBlock.getType().isSolid() &&
                    lBlock.getType() != Material.BARRIER) {
                break;
            }
            if (cBlock.getType() == Material.AIR && lBlock.getType() == Material.WATER) {
                break;
            }
            if (cBlock.getType() == Material.AIR && tBlock.getType() == Material.AIR &&
                    lBlock.getType().isSolid() && lBlock.getType() != Material.BARRIER
            ) {
                location.setY(y);
                newLocation = location;
                break;
            }
        }
        return newLocation;
    }

    public static boolean teleportPlayerToSafeOrderedHeight(Player player, Location location) {
        List<Integer> ys = Stream.iterate(2, n -> n + 1).limit(location.getWorld().hasCeiling() ? 100 : 200)
                .sorted(Collections.reverseOrder()).collect(Collectors.toList());
        Location newLocation = getSafeLocation(location, ys);
        if (newLocation != null) {
            player.teleport(newLocation);
        }
        return newLocation != null;
    }

    public static void teleportPlayerToRandomLocationInRadius(Player player, Location center, int radius) {
        boolean validLocation = false;
        int currentAttempts = 100;
        while (!validLocation && currentAttempts-- > 0) {
            Location location = center.clone().add(random.nextInt(radius * 2) - radius, 0,
                    random.nextInt(radius * 2) - radius);
            validLocation = teleportPlayerToSafeOrderedHeight(player, location);
        }
        if (!validLocation || currentAttempts <= 0) {
            sendServerMessage(player, Component.text("No se pudo tele transportar a una ubicación segura",
                    TextColor.color(0xE38486)));
            center.setY(center.getWorld().hasCeiling() ? 60 : 120);
            player.teleport(center);
        }
    }

}
