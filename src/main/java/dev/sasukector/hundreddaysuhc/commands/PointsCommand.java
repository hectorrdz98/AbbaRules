package dev.sasukector.hundreddaysuhc.commands;

import dev.sasukector.hundreddaysuhc.controllers.PointsController;
import dev.sasukector.hundreddaysuhc.helpers.ServerUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PointsCommand implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
              ServerUtilities.sendServerMessage(player, "Tienes " + PointsController.getInstance().getPlayerPoint(player) +
                      " puntos, estás en la posición #" + PointsController.getInstance().getPlayerPosition(player));
            } else {
                String option = args[0];
                if (validOptions(player).contains(option)) {
                    switch (option) {
                        case "top" -> {
                            LinkedHashMap<UUID, Integer> top10 = PointsController.getInstance().getTop10();
                            player.playSound(player.getLocation(), "minecraft:block.note_block.bell", 1, 1);
                            Component message = Component.newline().append(ServerUtilities.getMiniMessage().parse(
                                    "<bold><gradient:#4CC9F0:#560BAD>- Top 10 -</gradient></bold>"
                            ));
                            int count = 1;
                            for (Map.Entry<UUID, Integer> entry : top10.entrySet()) {
                                String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                                if (playerName != null) {
                                    message = message.append(Component.newline())
                                            .append(Component.text(count + ". ", TextColor.color(0xFFFFFF)))
                                            .append(Component.text(playerName, TextColor.color(0xB5179E)))
                                            .append(Component.text(" " + entry.getValue(), TextColor.color(0xF72585)));
                                }
                                count++;
                            }
                            ServerUtilities.sendServerMessage(player, message);
                        }
                        case "load" -> {
                            if (player.isOp()) {
                                player.playSound(player.getLocation(), "minecraft:block.note_block.bell", 1, 1);
                                PointsController.getInstance().loadPointsFromFile();
                                ServerUtilities.sendServerMessage(player, "Se cargaron los puntos del archivo");
                            } else {
                                ServerUtilities.sendServerMessage(player, "§cPermisos insuficientes");
                            }
                        }
                        case "save" -> {
                            if (player.isOp()) {
                                player.playSound(player.getLocation(), "minecraft:block.note_block.bell", 1, 1);
                                PointsController.getInstance().savePointsToFile();
                                ServerUtilities.sendServerMessage(player, "Se guardaron los puntos al archivo");
                            } else {
                                ServerUtilities.sendServerMessage(player, "§cPermisos insuficientes");
                            }
                        }
                        case "add" -> {
                            if (player.isOp()) {
                                if (args.length > 2) {
                                    Player otherPlayer = Bukkit.getPlayer(args[1]);
                                    String preAmount = args[2];
                                    if (otherPlayer != null) {
                                        player.playSound(player.getLocation(), "minecraft:block.note_block.bell", 1, 1);
                                        PointsController.getInstance().addPointsToPlayer(otherPlayer, Integer.parseInt(preAmount));
                                        ServerUtilities.sendServerMessage(player, "Se agregaron " + preAmount + " puntos al jugador " + otherPlayer.getName());
                                    } else {
                                        ServerUtilities.sendServerMessage(player, "§cJugador no encontrado");
                                    }
                                } else {
                                    ServerUtilities.sendServerMessage(player, "§cComando incompleto");
                                }
                            } else {
                                ServerUtilities.sendServerMessage(player, "§cPermisos insuficientes");
                            }
                        }
                    }
                } else {
                    ServerUtilities.sendServerMessage(player, "§cOpción inválida");
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if(sender instanceof Player player) {
            if (args.length == 1) {
                String partialItem = args[0];
                StringUtil.copyPartialMatches(partialItem, validOptions(player), completions);
            }
        }

        Collections.sort(completions);

        return completions;
    }

    public List<String> validOptions(Player player) {
        List<String> valid = new ArrayList<>();
        if (player.isOp()) {
            valid.add("load");
            valid.add("save");
            valid.add("add");
        }
        valid.add("top");
        Collections.sort(valid);
        return valid;
    }
}
