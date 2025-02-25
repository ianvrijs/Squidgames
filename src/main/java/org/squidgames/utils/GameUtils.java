package org.squidgames.utils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.handlers.PlayerStateHandler;

import java.util.ArrayList;
import java.util.List;

public class GameUtils {
    /**
     * Starts a countdown for the game to start.
     *
     * @param plugin the plugin instance
     * @param seconds the number of seconds to countdown
     * @param queuedPlayers the list of players in the queue
     * @param onComplete the action to run when the countdown is complete
     */
    public static void startCountdown(SquidGamesPlugin plugin, int seconds, List<Player> queuedPlayers, Runnable onComplete) {
        new BukkitRunnable() {
            int countdown = seconds;

            @Override
            public void run() {
                if (countdown <= 0) {
                    for (Player player : queuedPlayers) {
                        player.sendTitle(ChatColor.GREEN + "GO!", ChatColor.YELLOW + "Good luck!", 10, 20, 10);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON, 1.0f, 1.0f);
                    }
                    onComplete.run();
                    cancel();
                    return;
                }

                for (Player player : queuedPlayers) {
                    player.sendTitle(ChatColor.GREEN + "Game starts in", ChatColor.YELLOW + String.valueOf(countdown), 10, 20, 10);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20); // 1s
    }

    public static void fillInventoryWithWool(List<Player> queuedPlayers, boolean isRedLight, PlayerStateHandler playerStateHandler) {
        Material woolColor = isRedLight ? Material.RED_WOOL : Material.GREEN_WOOL;
        ItemStack wool = new ItemStack(woolColor, 1);
        for (Player player : queuedPlayers) {
            if (!playerStateHandler.isPlayerSafe(player) && !playerStateHandler.isPlayerDead(player)) {
                for (int i = 0; i < player.getInventory().getSize(); i++) {
                    if (i >= 36 && i <= 39) {
                        continue;
                    }
                    player.getInventory().setItem(i, wool);
                }
                player.getInventory().setItemInOffHand(wool);
            }
        }
    }

    public static void displayEndGameResults(PlayerStateHandler playerStateHandler, List<Player> queuedPlayers) {
        List<Player> safePlayers = new ArrayList<>(playerStateHandler.getSafePlayers());
        if (safePlayers.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "Game Over! No winners.");
            return;
        }
        Bukkit.broadcastMessage(ChatColor.GOLD + "Game Over! Winners:");
        for (int i = 0; i < Math.min(safePlayers.size(), 10); i++) {
            Player player = safePlayers.get(i);
            Bukkit.broadcastMessage(String.format("%s%d. %s", ChatColor.GREEN, (i + 1), player.getName()));
        }
        for (Player player : queuedPlayers) {
            if (playerStateHandler.isPlayerSafe(player)) {
                player.sendTitle(ChatColor.GOLD + "Game Over!", ChatColor.GREEN + "Congratulations!", 10, 70, 20);
            } else {
                player.sendTitle(ChatColor.GOLD + "Game Over!", ChatColor.RED + "Better luck next time..", 10, 70, 20);
            }
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public static void startPvpCountdown(SquidGamesPlugin plugin, int seconds, List<Player> queuedPlayers, Runnable onComplete) {
        new BukkitRunnable() {
            int countdown = seconds;

            @Override
            public void run() {
                if (countdown <= 0) {
                    for (Player player : queuedPlayers) {
                        player.sendTitle(ChatColor.RED + "PvP Enabled!", ChatColor.YELLOW + "Fight for your life!", 10, 20, 10);
                        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                    }
                    onComplete.run();
                    cancel();
                    return;
                }

                for (Player player : queuedPlayers) {
                    player.sendTitle(ChatColor.RED + "PvP starts in", ChatColor.YELLOW + String.valueOf(countdown), 10, 20, 10);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    player.spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20); // 1s
    }
}