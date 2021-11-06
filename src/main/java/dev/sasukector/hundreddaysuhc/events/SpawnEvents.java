package dev.sasukector.hundreddaysuhc.events;

import dev.sasukector.hundreddaysuhc.controllers.BoardController;
import dev.sasukector.hundreddaysuhc.controllers.GameController;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class SpawnEvents implements Listener {

    private static final Map<Material, Integer> ores;
    static {
        ores = new HashMap<>();
        ores.put(Material.COAL_ORE, 1);
        ores.put(Material.COPPER_ORE, 2);
        ores.put(Material.DIAMOND_ORE, 6);
        ores.put(Material.GOLD_ORE, 3);
        ores.put(Material.IRON_ORE, 2);
        ores.put(Material.EMERALD_ORE, 8);
        ores.put(Material.LAPIS_ORE, 3);
        ores.put(Material.REDSTONE_ORE, 2);
        ores.put(Material.NETHER_GOLD_ORE, 3);
        ores.put(Material.NETHER_QUARTZ_ORE, 1);
        ores.put(Material.DEEPSLATE_COAL_ORE, 1);
        ores.put(Material.DEEPSLATE_COPPER_ORE, 2);
        ores.put(Material.DEEPSLATE_DIAMOND_ORE, 6);
        ores.put(Material.DEEPSLATE_GOLD_ORE, 3);
        ores.put(Material.DEEPSLATE_IRON_ORE, 2);
        ores.put(Material.DEEPSLATE_EMERALD_ORE, 8);
        ores.put(Material.DEEPSLATE_LAPIS_ORE, 3);
        ores.put(Material.DEEPSLATE_REDSTONE_ORE, 2);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(
                Component.text("+ ", TextColor.color(0x84E3A4))
                        .append(Component.text(player.getName(), TextColor.color(0x84E3A4)))
        );
        BoardController.getInstance().newPlayerBoard(player);
        GameController.getInstance().handlePlayerJoin(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        BoardController.getInstance().removePlayerBoard(player);
        event.quitMessage(
                Component.text("- ", TextColor.color(0xE38486))
                        .append(Component.text(player.getName(), TextColor.color(0xE38486)))
        );
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!GameController.getInstance().isGameStarted() && !event.getPlayer().isOp()) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getZ() != to.getZ() || from.getY() != to.getY()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!GameController.getInstance().isGameStarted()) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        } else {
            Block block = event.getBlock();
            if (ores.containsKey(block.getType())) {
                event.setDropItems(false);
                GameController.getInstance().onPlayerScore(event.getPlayer(), ores.get(block.getType()));
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!GameController.getInstance().isGameStarted()) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockChestInteract(PlayerInteractEvent event) {
        if (!GameController.getInstance().isGameStarted()) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (block != null && block.getState() instanceof InventoryHolder) {
                    if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                        event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDroppedItem(PlayerDropItemEvent event) {
        if (!GameController.getInstance().isGameStarted()) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemEaten(PlayerItemConsumeEvent event) {
        if (!GameController.getInstance().isGameStarted()) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickedUpItems(PlayerAttemptPickupItemEvent event) {
        if (!GameController.getInstance().isGameStarted()) {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.deathMessage(Component.text(player.getName() + " ha muerto...", TextColor.color(0xF94144)));
    }

    @EventHandler
    public void onPortalEntered(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

}
