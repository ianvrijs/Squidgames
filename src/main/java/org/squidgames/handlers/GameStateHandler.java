package org.squidgames.handlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.squidgames.GameState;
import org.squidgames.SquidGamesPlugin;
import org.squidgames.setup.SetLightCommand;
import org.squidgames.utils.GameUtils;

import java.util.*;

public class GameStateHandler {
    private final Map<UUID, Long> lastActivityTime = new HashMap<>();
    private static final long AFK_TIMEOUT = 3 * 60 * 1000; // 3 mins in ms

    private final SquidGamesPlugin plugin;
    private final PlayerStateHandler playerStateHandler;
    private final SetLightCommand setLightCommand;
    private GameState currentState;
    private boolean isRedLight;
    private final List<Player> queuedPlayers = new ArrayList<>();

    public GameStateHandler(SquidGamesPlugin plugin) {
        this.plugin = plugin;
        this.playerStateHandler = new PlayerStateHandler();
        this.setLightCommand = new SetLightCommand(plugin);
        this.currentState = GameState.LOBBY;
        this.isRedLight = false;
        updateLightColor();
    }

    public void startGame(CommandSender sender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!playerStateHandler.isPlayerExempt(player)) {
                queuedPlayers.add(player);
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
        } else if (currentState == GameState.PLAYING) {
            sender.sendMessage(ChatColor.RED + "Game is already running.");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Game started!");
        currentState = GameState.STARTING;
        updateLightColor(); //briefly change lights to yellow to indicate starting state
        GameUtils.startCountdown(plugin, 5, queuedPlayers, () -> {
            currentState = GameState.PLAYING;
            updateLightColor(); //light to green
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
        });
    }

    public void stopGame(CommandSender sender) {
        if (currentState != GameState.PLAYING) {
            sender.sendMessage(ChatColor.RED + "No game running.");
            return;
        }
        sender.sendMessage(ChatColor.RED + "Game stopped!");
        currentState = GameState.STOPPED;
        updateLightColor();
        plugin.getPlayerMovementListener().removeAllCorpses();
        Location lobbyLocation = getLobbyLocation();
        for (Player player : queuedPlayers) {
            player.getInventory().clear();
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }
        }
        queuedPlayers.clear(); //empty queued players list
        Bukkit.getLogger().info("Game state set to STOPPED.");
    }

    public void playerDied(Player player) {
        if (currentState == GameState.PLAYING) {
            playerStateHandler.markPlayerAsDead(player);
            player.getInventory().clear();
            Location lobbyLocation = getLobbyLocation();
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation);
            }
            checkGameEnd();
        }
    }

    public GameState getCurrentState() { return currentState; }
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
                String message = isRedLight ? ChatColor.RED + "Red Light!" : ChatColor.GREEN + "Green Light!";
                updateLightColor();

                //send red/green light message to enqueued players
                for (Player player : queuedPlayers) {
                    if (!playerStateHandler.isPlayerSafe(player) && !playerStateHandler.isPlayerDead(player)) {
                        player.sendMessage(message);

                    }
                }
                //keep track of time to calc/ add player reaction time
                if (isRedLight) {
                    plugin.getPlayerMovementListener().setRedLightStartTime(System.currentTimeMillis());
                }

                GameUtils.fillInventoryWithWool(queuedPlayers, isRedLight, playerStateHandler);

                boolean useRandomInterval = plugin.getConfig().getBoolean("useRandomInterval", false);
                long interval = useRandomInterval ? (2 + (int) (Math.random() * 5)) * 20 : 100; // 2 to 6 seconds in ticks
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        startRedLightGreenLightGame();
                    }
                }.runTaskLater(plugin, interval);
            }
        }.runTaskLater(plugin, 0); // Initial delay of 0 ticks
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

    private Location getArenaLocation() {
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

    private Location getLobbyLocation() {
        if (!plugin.getConfig().contains("lobby")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("lobby.world");
        double x = plugin.getConfig().getDouble("lobby.x");
        double y = plugin.getConfig().getDouble("lobby.y");
        double z = plugin.getConfig().getDouble("lobby.z");
        assert worldName != null;
        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    private Location getSpawnLocation() {
        if (!plugin.getConfig().contains("spawn")) {
            return null;
        }
        String worldName = plugin.getConfig().getString("spawn.world");
        double x = plugin.getConfig().getDouble("spawn.x");
        double y = plugin.getConfig().getDouble("spawn.y");
        double z = plugin.getConfig().getDouble("spawn.z");
        assert worldName != null;
        return new Location(Bukkit.getWorld(worldName), x, y, z);
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
        player.sendMessage(ChatColor.GREEN + "You're safe!");
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

            new BukkitRunnable() {
                @Override
                public void run() {
                    Location lobbyLocation = getLobbyLocation();
                    if (lobbyLocation != null) {
                        stopGame(Bukkit.getConsoleSender());

                    }
                }
            }.runTaskLater(plugin, 100); //5s
        }
    }

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
        if(!Objects.equals(plugin.getConfig().getString("light.area.corner1.world"), plugin.getConfig().getString("arena.corner1.world"))) {
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
        for (Player player : queuedPlayers) {
            if (currentState == GameState.PLAYING && lastActivityTime.containsKey(player.getUniqueId())) {
                long lastActivity = lastActivityTime.get(player.getUniqueId());
                if (currentTime - lastActivity > AFK_TIMEOUT) {
                    playerDied(player);
                    player.sendMessage(ChatColor.RED + "You have been removed from the game due to inactivity.");
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
    public void removePlayerFromGame(Player player) {
        playerStateHandler.markPlayerAsDead(player);
        player.getInventory().clear();
        player.teleport(Objects.requireNonNull(getLobbyLocation()));
        player.sendMessage(ChatColor.RED + "You have been removed from the game.");
        checkGameEnd();
    }
    public List<Player> getQueuedPlayers() {
        return queuedPlayers;
    }
}