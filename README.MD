# Squid Games Plugin

This is a Minecraft plugin for a Squid Games-inspired game mode. The plugin is built using Java and Maven.


## Features

- Configurable game settings
- AFK player detection and handling
- Red light, green light gamemode
- Dynamic light colors
- Stats tracking for players
- Custom placeholders
- Easy to use admin commands

## Requirements

- Java 8 or higher
- Maven
- Bukkit/Spigot server

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/ianvrijs/Squidgames.git

    cd Squidgames


2. Build the plugin using Maven:
    ```sh
    mvn clean package
    ```

3. Copy the generated JAR file from the `target` directory to your server's `plugins` directory.

4. Start your Minecraft server.

## Usage and configuration

Before being able to start a game, you will have to set up the game area, safe zone, lobby, spawn point, and dynamic light area. You can do this by using the setup commands. 
It is recommended to seperate the lobby from the game area. 

### Configuration
 - `/sq setup setArena` - Set the game area by selecting two opposing corners.
 - `/sq setup setSafeZone` - Set the safe zone area by selecting two opposing corners.
 - `/sq setup setLobby` - Set the lobby spawn point to your current location.
 - `/sq setup setSpawn` - Set the game spawn point to your current location.
 - `/sq setup setLight` - Set the dynamic light area by selecting two opposing corners using the selection tool.
 - `/sq setup setRandom <true/false>` - Decide if the interval between red and green should be random or default (5s)
- `/sq setup setInterval <min> <max>` - Set the minimum and maximum interval between red and green light. A random interval will be chosen between these two values.

### Usage
- `/sq start` - Starts the game and creates the queue.
- `/sq stop` - Stops the game and empties the queue.
- `/sq remove <player>` - Remove a player from an ongoing game.
- `/sq exempt` - Toggles exempt mode for the command executor. Exempt players will be skipped over when creating the queue.
- `/sq clearstats` - Clears ALL player stats. Use with caution.
