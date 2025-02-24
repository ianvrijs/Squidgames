package org.squidgames.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.GameState;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.TabManager;
import org.squidgames.setup.SetLightCommand;
import org.squidgames.utils.GameUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;


import java.util.*;

public class GameStateHandler {
    private final Map<UUID, Long> lastActivityTime = new HashMap<>();
    private static final long AFK_TIMEOUT = 60 * 1000; // 1 min in ms

    private final SquidGamesPlugin plugin;
    private final PlayerStateHandler playerStateHandler;
    private final SetLightCommand setLightCommand;
    private final TabManager tabManager;
    private GameState currentState;
    private boolean isRedLight;
    private final List<Player> queuedPlayers = new ArrayList<>();
    private boolean pvpEnabled;

    public GameStateHandler(SquidGamesPlugin plugin) {
        this.plugin = plugin;
        this.playerStateHandler = new PlayerStateHandler();
        this.setLightCommand = new SetLightCommand(plugin);
        this.currentState = GameState.LOBBY;
        this.isRedLight = false;
        this.tabManager = new TabManager(plugin);
        this.pvpEnabled = false;
        updateLightColor();
    }

    public void startGame(CommandSender sender) {
        if (!isConfigValid(sender)) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!playerStateHandler.isPlayerExempt(player)) {
                addPlayerToQueue(player);
            }
        }
        if (queuedPlayers.size() < 2) {
            sender.sendMessage(ChatColor.RED + "Not enough players to start the game.");
            return;
        } else if (getArenaLocation() == null) {
            sender.sendMessage(ChatColor.RED + "Arena location is not set.");
            return;
        } else if (getSpawnLocation() == null) {
            sender.sendMessage(ChatColor.RED + "Spawn location is not set.");
            return;
        } else if (getLobbyLocation() == null) {
            sender.sendMessage(ChatColor.RED + "Lobby location is not set.");
            return;
        } else if (currentState == GameState.PLAYING || currentState == GameState.STARTING) {
            sender.sendMessage(ChatColor.RED + "Game is already running.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Game started!");
        currentState = GameState.STARTING;
        updateLightColor(); // briefly change lights to yellow to indicate starting state
        registerListeners();
        for (Player player : queuedPlayers) {
            player.setHealth(20);
            player.setFoodLevel(20);
        }
        startAppropriateCountdown();
    }

    private void startAppropriateCountdown() {
        if (pvpEnabled) {
            GameUtils.startPvpCountdown(plugin, 5, queuedPlayers, this::onCountdownComplete);
        } else {
            GameUtils.startCountdown(plugin, 5, queuedPlayers, this::onCountdownComplete);
        }
    }

    private void onCountdownComplete() {
        currentState = GameState.PLAYING;
        updateLightColor(); // light to green
        playerStateHandler.resetPlayerStates();
        Location spawnLocation = getSpawnLocation();
        for (Player player : queuedPlayers) {
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
            }
            giveCyanLeatherOutfit(player);
        }
        Bukkit.getLogger().info("Game state set to PLAYING and player states reset.");
        startRedLightGreenLightGame();
    }

    public void stopGame(CommandSender sender) {
        if (currentState != GameState.PLAYING && currentState != GameState.STARTING) {
            sender.sendMessage(ChatColor.RED + "No game running.");
            return;
        }
        sender.sendMessage(ChatColor.RED + "Game stopped!");
        currentState = GameState.STOPPED;
        updateLightColor();
        unregisterListeners();
        resetAllPlayerTabColors();
        plugin.getPlayerMovementListener().removeAllCorpses();
        Location lobbyLocation = getLobbyLocation();
        for (Player player : queuedPlayers) {
            player.getInventory().clear();
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }
        }
        queuedPlayers.clear();
        Bukkit.getLogger().info("Game state set to STOPPED.");
    }

    public void playerDied(Player player) {
        if (currentState == GameState.PLAYING) {
            playerStateHandler.markPlayerAsDead(player);
            player.getInventory().clear();
            Location lobbyLocation = getLobbyLocation();
            handlePlayerElimination(player);
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public boolean isRedLight() {
        return isRedLight;
    }

    private void startRedLightGreenLightGame() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentState != GameState.PLAYING) {
                    cancel();
                    return;
                }

                isRedLight = !isRedLight;
                String message = isRedLight ? ChatColor.RED + "" + ChatColor.BOLD + "Red Light!" : ChatColor.GREEN + "" + ChatColor.BOLD + "Green Light!";                updateLightColor();

                for (Player player : queuedPlayers) {
                    if (!playerStateHandler.isPlayerSafe(player) && !playerStateHandler.isPlayerDead(player)) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                    }
                    if (isRedLight) {
                        player.playSound(player.getLocation(), "minecraft:block.note_block.bell", 1.0f, 1.0f);
                    } else {
                        player.playSound(player.getLocation(), "minecraft:entity.villager.no", 1.0f, 1.0f);
                    }
                }
                if (isRedLight) {
                    plugin.getPlayerMovementListener().setRedLightStartTime(System.currentTimeMillis());
                }

                GameUtils.fillInventoryWithWool(queuedPlayers, isRedLight, playerStateHandler);

                if (isPvpEnabled()) {
                    for (Player player : queuedPlayers) {
                        player.setInvulnerable(false);
                    }
                }
                boolean useRandomInterval = plugin.getConfig().getBoolean("useRandomInterval", false);
                int minSeconds = plugin.getConfig().getInt("interval.min", 2);
                int maxSeconds = plugin.getConfig().getInt("interval.max", 6);
                long interval = useRandomInterval ? (minSeconds + (int) (Math.random() * (maxSeconds - minSeconds + 1))) * 20 : 100;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        startRedLightGreenLightGame();
                    }
                }.runTaskLater(plugin, interval);
            }
        }.runTaskLater(plugin, 0);
    }

    private void giveCyanLeatherOutfit(Player player) {
        ItemStack[] armor = new ItemStack[4];
        armor[0] = createDyedArmor(Material.LEATHER_BOOTS);
        armor[1] = createDyedArmor(Material.LEATHER_LEGGINGS);
        armor[2] = createDyedArmor(Material.LEATHER_CHESTPLATE);
        armor[3] = createDyedArmor(Material.LEATHER_HELMET);
        player.getInventory().setArmorContents(armor);
    }

    private ItemStack createDyedArmor(Material material) {
        ItemStack item = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(Color.fromRGB(46, 119, 87));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Location getArenaLocation() {
        if (!plugin.getConfig().contains("arena.corner1")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("arena.corner1.world");
        double x = plugin.getConfig().getDouble("arena.corner1.x");
        double y = plugin.getConfig().getDouble("arena.corner1.y");
        double z = plugin.getConfig().getDouble("arena.corner1.z");
        assert worldName != null;
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }
    public Location getArenaCorner1() {
        if (!plugin.getConfig().contains("arena.corner1")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("arena.corner1.world");
        if (worldName == null) {
            return null;
        }
        double x = plugin.getConfig().getDouble("arena.corner1.x");
        double y = plugin.getConfig().getDouble("arena.corner1.y");
        double z = plugin.getConfig().getDouble("arena.corner1.z");
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public Location getArenaCorner2() {
        if (!plugin.getConfig().contains("arena.corner2")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("arena.corner2.world");
        if (worldName == null) {
            return null;
        }
        double x = plugin.getConfig().getDouble("arena.corner2.x");
        double y = plugin.getConfig().getDouble("arena.corner2.y");
        double z = plugin.getConfig().getDouble("arena.corner2.z");
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public Location getLobbyLocation() {
        if (!plugin.getConfig().contains("lobby")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("lobby.world");
        if (worldName == null) {
            plugin.getLogger().severe("Lobby world name is not set in the config.");
            return null;
        }
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        float yaw = (float) plugin.getConfig().getDouble("lobby.yaw");
        float pitch = (float) plugin.getConfig().getDouble("lobby.pitch");
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public Location getSpawnLocation() {
        if (!plugin.getConfig().contains("spawn")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("spawn.world");
        if (worldName == null) {
            plugin.getLogger().severe("Spawn world name is not set in the config.");
            return null;
        }
        double x = plugin.getConfig().getDouble("spawn.x");
        double y = plugin.getConfig().getDouble("spawn.y");
        double z = plugin.getConfig().getDouble("spawn.z");
        float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
        float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public PlayerStateHandler getPlayerStateHandler() {
        return playerStateHandler;
    }

    public void markPlayerAsSafe(Player player) {
        if (playerStateHandler.isPlayerSafe(player)) {
            return;
        }
        player.getInventory().clear();
        playerStateHandler.markPlayerAsSafe(player);
        updatePlayerTabColor(player);
        handlePlayerFinish(player);
        checkGameEnd();
    }
    private void checkGameEnd() {
        boolean allSafeOrEliminated = true;
        for (Player player : queuedPlayers) {
            if (!playerStateHandler.isPlayerSafe(player) && !playerStateHandler.isPlayerDead(player)) {
                allSafeOrEliminated = false;
                break;
            }
        }
        if (allSafeOrEliminated) {
            GameUtils.displayEndGameResults(playerStateHandler, queuedPlayers);
            List<Player> safePlayers = new ArrayList<>(playerStateHandler.getSafePlayers());
            plugin.getStatsManager().saveAllQueuedPlayers(queuedPlayers);
            for (Player player : queuedPlayers) {
                boolean isWinner = safePlayers.contains(player);
                int rank = safePlayers.indexOf(player) + 1;
                plugin.getStatsManager().updatePlayerStats(player, isWinner, rank);
                plugin.getScoreboardManager().setupScoreboard(player);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location lobbyLocation = getLobbyLocation();
                    if (lobbyLocation != null) {
                        stopGame(Bukkit.getConsoleSender());
                    }
                }
            }.runTaskLater(plugin, 100); // 5s
        } }


    private void updateLightColor() {
        Location lightLocation = getLightLocation();
        if (lightLocation == null) {
            return;
        }
        Material color = switch (currentState) {
            case PLAYING -> isRedLight ? Material.RED_WOOL : Material.GREEN_WOOL;
            case STARTING -> Material.YELLOW_WOOL;
            default -> Material.WHITE_WOOL;
        };

        setLightCommand.setLightColor(color);
    }

    private Location getLightLocation() {
        if (!plugin.getConfig().contains("light.area.corner1")) {
            return null;
        }
        if (!Objects.equals(plugin.getConfig().getString("light.area.corner1.world"), plugin.getConfig().getString("arena.corner1.world"))) {
            return null;
        }
        String worldName = plugin.getConfig().getString("light.area.corner1.world");
        if (worldName == null) {
            plugin.getLogger().severe("Light world name is not set in the config.");
            return null;
        }
        double x = plugin.getConfig().getDouble("light.area.corner1.x");
        double y = plugin.getConfig().getDouble("light.area.corner1.y");
        double z = plugin.getConfig().getDouble("light.area.corner1.z");
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    public void setUseRandomInterval(boolean useRandomInterval) {
        plugin.getConfig().set("useRandomInterval", useRandomInterval);
        plugin.saveConfig();
    }

    public void updatePlayerActivity(Player player) {
        lastActivityTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void checkForAfkPlayers() {
        long currentTime = System.currentTimeMillis();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (lastActivityTime.containsKey(player.getUniqueId())) {
                long lastActivity = lastActivityTime.get(player.getUniqueId());
                if (currentTime - lastActivity > AFK_TIMEOUT) {
                    if (currentState == GameState.PLAYING && !playerStateHandler.isPlayerSafe(player) && !playerStateHandler.isPlayerDead(player)) {
                        //mark leftover players as dead when game is ongoing
                        playerDied(player);
                        player.sendMessage(ChatColor.RED + "You've been eliminated due to a lack of activity.");
                        }
                    }
                }
            }
        }

    public void exemptPlayer(Player player) {
        if (playerStateHandler.isPlayerExempt(player)) {
            playerStateHandler.unmarkPlayerAsExempt(player);
            player.sendMessage(ChatColor.GREEN + "You are no longer exempt from match making.");
        } else {
            playerStateHandler.markPlayerAsExempt(player);
            player.sendMessage(ChatColor.GREEN + "You have been exempted from match making.");
        }
    }

    public List<Player> getQueuedPlayers() {
        return queuedPlayers;
    }

    public void handlePlayerElimination(Player player) {
        String deathMessage = ChatColor.RED + "☠ " + player.getName();
        Bukkit.broadcastMessage(deathMessage);
        updatePlayerTabColor(player);
        checkGameEnd();
    }

    public void handlePlayerFinish(Player player) {
        String finishMessage = ChatColor.GREEN + "🏆 " + player.getName();
        Bukkit.broadcastMessage(finishMessage);
        player.sendMessage(ChatColor.GREEN + "You made it!");
    }
    private boolean isConfigValid(CommandSender sender) {
        if (!plugin.getConfig().contains("arena.corner1") || !plugin.getConfig().contains("arena.corner2")) {
            sender.sendMessage(ChatColor.RED + "Arena corners are not set in the config. | </sq setup setarena>");
            return false;
        }
        if (!plugin.getConfig().contains("safezone.corner1") || !plugin.getConfig().contains("safezone.corner2")) {
            sender.sendMessage(ChatColor.RED + "Safezone corners are not set in the config. | </sq setup setsafezone>");
            return false;
        }
        if (!plugin.getConfig().contains("lobby")) {
            sender.sendMessage(ChatColor.RED + "Lobby location is not set in the config.| </sq setup setlobby>");
            return false;
        }
        if (!plugin.getConfig().contains("spawn")) {
            sender.sendMessage(ChatColor.RED + "Spawn location is not set in the config. | </sq setup setspawn>");
            return false;
        }
        if (!plugin.getConfig().contains("light.area.corner1") || !plugin.getConfig().contains("light.area.corner2")) {
            sender.sendMessage(ChatColor.RED + "Light area corners are not set in the config. | </sq setup setlight>");
            return false;
        }
        return true;
    }
    public void removeQueuedPlayer(Player player) {
        if(queuedPlayers.contains(player)) {
            queuedPlayers.remove(player);
            Bukkit.getLogger().info("Player " + player.getName() + " removed from the queue.");
        }
    }
    public void addPlayerToQueue(Player player) {
        if (!queuedPlayers.contains(player) && !playerStateHandler.isPlayerExempt(player)) {
            queuedPlayers.add(player);
            Bukkit.getLogger().info("Player " + player.getName() + " added to the queue.");
        }
    }

    public void registerListeners() {
        Bukkit.getLogger().info("Registering listeners...");

        Bukkit.getPluginManager().registerEvents(plugin.getPlayerMovementListener(), plugin);
        Bukkit.getPluginManager().registerEvents(plugin.getPlayerInventoryListener(), plugin);
        Bukkit.getPluginManager().registerEvents(plugin.getPlayerActivityListener(), plugin);
    }

    public void unregisterListeners() {
        Bukkit.getLogger().info("Unregistering listeners...");
        PlayerMoveEvent.getHandlerList().unregister(plugin.getPlayerMovementListener());
        InventoryClickEvent.getHandlerList().unregister(plugin.getPlayerInventoryListener());
        PlayerInteractEvent.getHandlerList().unregister(plugin.getPlayerActivityListener());
    }
    public void updatePlayerTabColor(Player player) {
        if (getPlayerStateHandler().isPlayerSafe(player)) {
            tabManager.setPlayerNameColor(player, ChatColor.GREEN);
        } else if (getPlayerStateHandler().isPlayerDead(player)) {
            tabManager.setPlayerNameColor(player, ChatColor.RED);
        } else {
            tabManager.setPlayerNameColor(player, ChatColor.GRAY);
        }
    }
    public void resetAllPlayerTabColors() {
        for (Player player : queuedPlayers) {
            tabManager.setPlayerNameColor(player, ChatColor.GRAY);
        }
    }
    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }
}