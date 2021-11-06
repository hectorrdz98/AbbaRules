package dev.sasukector.hundreddaysuhc.controllers;

import dev.sasukector.hundreddaysuhc.AbbaRules;
import dev.sasukector.hundreddaysuhc.helpers.FastBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class BoardController {

    private static BoardController instance = null;
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private @Setter @Getter boolean hideDays;

    public static BoardController getInstance() {
        if (instance == null) {
            instance = new BoardController();
        }
        return instance;
    }

    public BoardController() {
        Bukkit.getScheduler().runTaskTimer(AbbaRules.getInstance(), this::updateBoards, 0L, 20L);
        this.hideDays = false;
    }

    public void newPlayerBoard(Player player) {
        FastBoard board = new FastBoard(player);
        this.boards.put(player.getUniqueId(), board);
    }

    public void removePlayerBoard(Player player) {
        FastBoard board = this.boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    public void updateBoards() {
        boards.forEach((uuid, board) -> {
            Player player = Bukkit.getPlayer(uuid);
            assert player != null;

            board.updateTitle("§3§lAbba Rules");

            List<String> lines = new ArrayList<>();
            lines.add("");
            lines.add("Jugador: §6" + player.getName());
            lines.add("Puntos: §6" + PointsController.getInstance().getPlayerPoint(player));
            lines.add("Posición: §6" + PointsController.getInstance().getPlayerPosition(player));
            lines.add("");
            lines.add("Online: §6" + Bukkit.getOnlinePlayers().size());
            lines.add("Restante: §6" + PointsController.getInstance().getTimer() + " m");
            lines.add("");

            board.updateLines(lines);
        });
    }

}
