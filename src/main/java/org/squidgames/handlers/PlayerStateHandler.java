package org.squidgames.handlers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class PlayerStateHandler {
    private final Set<Player> deadPlayers = new HashSet<>();
    private final Set<Player> safePlayers = new LinkedHashSet<>();

    public void markPlayerAsDead(Player player) {
        deadPlayers.add(player);
    }

    public boolean isPlayerDead(Player player) {
        return deadPlayers.contains(player);
    }

    public void markPlayerAsSafe(Player player) {
        safePlayers.add(player);
    }

    public boolean isPlayerSafe(Player player) {
        return safePlayers.contains(player);
    }

    public void resetPlayerStates() {
        deadPlayers.clear();
        safePlayers.clear();
    }

    public Set<Player> getSafePlayers() {
        return safePlayers;
    }
}