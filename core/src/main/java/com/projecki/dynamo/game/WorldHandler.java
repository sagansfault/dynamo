package com.projecki.dynamo.game;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.fusion.game.state.GameState;
import com.projecki.fusion.map.config.ConfigWorld;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An internal use only object. This will attempt to look for an instantiated world "lobby". If one is not loaded but a
 * folder is present, it will construct one. If nothing is present then dynamo will throw an error
 */
public class WorldHandler {

    private final Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo;
    private final GameOptions gameOptions;

    private ConfigWorld selectedMap;
    private ConfigWorld selectedLobby;

    public WorldHandler(Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo, GameOptions gameOptions) {
        this.dynamo = dynamo;
        this.gameOptions = gameOptions;
    }

    public void loadWorlds() {

        File worldsDir = Bukkit.getWorldContainer();
        File[] worlds = worldsDir.listFiles();
        if (!worldsDir.exists() || worlds == null) {
            throw new IllegalStateException("Worlds folder doesnt exist or isn't a directory");
        }

        List<File> lobbies = new ArrayList<>();
        List<File> maps = new ArrayList<>();

        for (File world : worlds) {
            if (!world.isDirectory()) {
                continue;
            }
            String name = world.getName();

            boolean map = name.startsWith("map_");
            boolean lobby = name.startsWith("lobby_");
            if (map) {
                maps.add(world);
            } else if (lobby) {
                lobbies.add(world);
            }
        }

        if (maps.isEmpty()) {
            throw new IllegalStateException("No maps loaded");
        }

        if (lobbies.isEmpty()) {
            throw new IllegalStateException("No lobbies loaded");
        }

        File selectedLobby = lobbies.get(ThreadLocalRandom.current().nextInt(lobbies.size()));
        File selectedMap = maps.get(ThreadLocalRandom.current().nextInt(maps.size()));

        this.selectedLobby = this.handleLoad(selectedLobby)
                .orElseThrow(() -> new IllegalStateException("Could not load selected lobby: " + selectedLobby.getName()));
        this.selectedMap = this.handleLoad(selectedMap)
                .orElseThrow(() -> new IllegalStateException("Could not load selected map: " + selectedMap.getName()));

        dynamo.getLogger().info("Selected lobby: " + selectedLobby.getName());
        dynamo.getLogger().info("Selected map: " + selectedMap.getName());
    }

    private Optional<ConfigWorld> handleLoad(File file) {
        String name = file.getName();
        Optional<ConfigWorld> optionalWorld;
        if (name.endsWith("_dev")) {
            optionalWorld = this.cloneWorld(name);
        } else {
            optionalWorld = this.loadWorld(name);
        }
        return optionalWorld;
    }

    private Optional<ConfigWorld> loadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            File lobbyWorldFile = new File(Bukkit.getWorldContainer(), name);
            if (lobbyWorldFile.exists() && lobbyWorldFile.isDirectory()) {
                world = Bukkit.createWorld(WorldCreator.name(name));
            }
        }

        if (world == null) {
            return Optional.empty();
        }

        gameOptions.gameRules().formatWorld(world);

        Integer diameter = gameOptions.worldLoadingDiameters().get(name);
        return Optional.of(diameter == null ? ConfigWorld.fromWorld(world) : ConfigWorld.fromWorld(world, diameter));
    }

    private Optional<ConfigWorld> cloneWorld(String name) {
        File originalWorldFile = new File(Bukkit.getWorldContainer(), name);
        if (!originalWorldFile.exists() || !originalWorldFile.isDirectory()) {
            return Optional.empty();
        }

        String temporaryWorldName = "temp_" + name;
        File temporaryWorldFile = new File(Bukkit.getWorldContainer(), temporaryWorldName);

        try {
            if (temporaryWorldFile.exists()) {
                FileUtils.deleteDirectory(temporaryWorldFile); // Delete world from previous game
            }

            FileUtils.copyDirectory(originalWorldFile,
                    temporaryWorldFile,
                    new NotFileFilter(new NameFileFilter("uid.dat"))); // Excludes uid.dat to prevent error
        } catch (IOException e) {
            return Optional.empty();
        }

        return loadWorld(temporaryWorldName);
    }

    public ConfigWorld getSelectedMap() {
        return selectedMap;
    }

    public void setSelectedMap(ConfigWorld selectedMap) {
        this.selectedMap = selectedMap;
    }

    public ConfigWorld getSelectedLobby() {
        return selectedLobby;
    }

    public void setSelectedLobby(ConfigWorld selectedLobby) {
        this.selectedLobby = selectedLobby;
    }
}
