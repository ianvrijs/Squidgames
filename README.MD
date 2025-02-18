# Squid Games Plugin

This is a Minecraft plugin for a Squid Games-inspired game mode. The plugin is built using Java and Maven.

## Features

- Player state management (safe, dead, exempt)
- Arena, lobby, and spawn location management
- AFK player detection and handling
- Game state handling (starting, playing, ending)
- Light color updates based on game state
- Player inventory and armor management
- End game results display and player stats update

## Requirements

- Java 8 or higher
- Maven
- Bukkit/Spigot server

## Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/ianvrijs/Squidgames.git
    cd squidgames
    ```

2. Build the plugin using Maven:
    ```sh
    mvn clean package
    ```

3. Copy the generated JAR file from the `target` directory to your server's `plugins` directory.

4. Start your Minecraft server.

## Usage and configuration
 - Outline the safezone, lobby and light areas using the ``/sq setup`` sub-commands
 - Set the spawn area using the ``/sq setspawn`` command
 - Start the game using the ``/sq start`` command

