package dev.sasukector.hundreddaysuhc.controllers;

import dev.sasukector.hundreddaysuhc.AbbaRules;
import dev.sasukector.hundreddaysuhc.helpers.ServerUtilities;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;

public class GameController {

    private static GameController instance = null;
    private @Getter @Setter boolean gameStarted;
    private @Getter @Setter int remainingTime;
    private @Getter int schedulerID = -1;

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public GameController() {
        this.gameStarted = false;
        this.remainingTime = 15 * 60;
    }

    public void restartPlayer(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setLevel(0);
        player.setArrowsInBody(0);
        player.setFireTicks(0);
        player.setVisualFire(false);
        player.getActivePotionEffects().forEach(p -> player.removePotionEffect(p.getType()));
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 99999, 0));
        player.getInventory().clear();
        player.updateInventory();
    }

    public void givePlayerKit(Player player) {
        ItemStack pickaxe = new ItemStack(Material.NETHERITE_PICKAXE);
        pickaxe.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
        ItemMeta pickaxeMeta = pickaxe.getItemMeta();
        pickaxeMeta.displayName(Component.text("Pico", TextColor.color(0xB5179E)));
        pickaxeMeta.setUnbreakable(true);
        pickaxe.setItemMeta(pickaxeMeta);
        player.getInventory().addItem(pickaxe);

        ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
        shovel.addUnsafeEnchantment(Enchantment.DIG_SPEED, 5);
        ItemMeta shovelMeta = pickaxe.getItemMeta();
        shovelMeta.displayName(Component.text("Pala", TextColor.color(0x4895EF)));
        shovelMeta.setUnbreakable(true);
        shovel.setItemMeta(shovelMeta);
        player.getInventory().addItem(shovel);

        player.getInventory().addItem(new ItemStack(Material.TORCH, 10));
        player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
        player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));

        player.updateInventory();
    }

    public void handlePlayerJoin(Player player) {
        if (!this.isGameStarted()) {
            player.setGameMode(GameMode.SPECTATOR);
            this.restartPlayer(player);
            player.teleport(ServerUtilities.getLobbySpawn());
        }
    }

    public void onPlayerScore(Player player, int points) {
        player.sendActionBar(ServerUtilities.getMiniMessage().parse(
                "<color:#4CC9F0>+ " + points + "</color>"
        ));
        PointsController.getInstance().addPointsToPlayer(player, points);
        int goal = 0;
        int playerPoints = PointsController.getInstance().getPlayerPoint(player);
        switch (playerPoints) {
            case 100, 250, 500, 1000, 2500, 5000, 10000 -> goal = playerPoints;
        }
        if (goal > 0) {
            ServerUtilities.sendBroadcastMessage(ServerUtilities.getMiniMessage().parse(
                    "<bold><color:#4895EF>" + player.getName() +
                            "</color></bold> <color:#480CA8>ha acumulado " + goal + " puntos</color>"
            ));
        }
    }

    public void gameStart() {
        this.remainingTime = 15 * 60;
        this.gameStarted = false;
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.setStatistic(Statistic.PLAYER_KILLS, 0);
            p.setGameMode(GameMode.SURVIVAL);
            this.restartPlayer(p);
            this.givePlayerKit(p);
            ServerUtilities.teleportPlayerToRandomLocationInRadius(p, ServerUtilities.getLobbySpawn(), 2000);
            p.playSound(p.getLocation(), "minecraft:block.note_block.xylophone", 1, 1);
        });
        this.startCountDown();
    }

    public void gameStop() {
        this.gameStarted = false;
        if (this.schedulerID != -1) {
            Bukkit.getScheduler().cancelTask(this.schedulerID);
        }
        PointsController.getInstance().savePointsToFile();
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.setGameMode(GameMode.SPECTATOR);
            this.restartPlayer(p);
            p.teleport(ServerUtilities.getLobbySpawn());
            p.playSound(p.getLocation(), "minecraft:entity.wither.death", 1, 1.6f);
        });
    }

    public void startCountDown() {
        AtomicInteger remainingTime = new AtomicInteger(20);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime.get() <= 0) {
                    gameStarted = true;
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        p.showTitle(Title.title(Component.text("¡A minar!", TextColor.color(0xB5179E)), Component.empty()));
                        p.playSound(p.getLocation(), "minecraft:entity.wither.spawn", 1, 1.6f);
                    });
                    startScheduler();
                    cancel();
                } else {
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        if (remainingTime.get() <= 3) {
                            p.showTitle(Title.title(Component.text(remainingTime.get(), TextColor.color(0xB5179E)), Component.empty()));
                            p.playSound(p.getLocation(), "minecraft:block.note_block.xylophone", 1, 1);
                        }
                        p.sendActionBar(
                                Component.text("La partida empieza en " + remainingTime.get() + "s",
                                        TextColor.color(0xF72585))
                        );
                    });
                    remainingTime.addAndGet(-1);
                }
            }
        }.runTaskTimer(AbbaRules.getInstance(), 0L, 20L);
    }

    public void startScheduler() {
        this.schedulerID = new BukkitRunnable() {
            @Override
            public void run() {
                if (--remainingTime <= 0) {
                    gameStop();
                } else if (remainingTime <= 10) {
                    Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), "minecraft:block.note_block.xylophone", 1, 1));
                } else if (remainingTime % 60 == 0) {
                    PointsController.getInstance().savePointsToFile();
                }
            }
        }.runTaskTimer(AbbaRules.getInstance(), 0L, 20L).getTaskId();
    }

}
