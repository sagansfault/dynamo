package com.projecki.dynamo;

import co.aikar.commands.PaperCommandManager;
import com.projecki.dynamo.command.DynamoCommand;
import com.projecki.dynamo.game.Game;
import com.projecki.dynamo.game.WorldHandler;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.state.LimboState;
import com.projecki.dynamo.state.LobbyState;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.control.ControlAction;
import com.projecki.fusion.game.GameType;
import com.projecki.fusion.game.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class Dynamo<T extends GameFrame<? extends GameState>, K extends PlayerHandler> extends JavaPlugin {

    private static Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> instance;

    private Engine<T, K> engine;
    private static final AtomicLong UPDATE_ID = new AtomicLong();

    private static PaperCommandManager commandManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        engine.getInternals().getStateMachine().changeState(new LimboState(this));
        if (engine == null || engine.getGame() == null) {
            getLogger().severe("Game not loaded into dynamo, remaining in limbo state.");
            return;
        }

        // events have to be registered in onEnable or later
        engine.getGame().getDeathMessagePipeline().registerEvents();

        WorldHandler worldHandler = engine.getWorldHandler();
        worldHandler.loadWorlds();
        engine.getInternals().getStateMachine().changeState(new LobbyState(this));

        // register command
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new DynamoCommand());

        // heart beat info to redis
        beginHeartbeat();

        // register control handlers
        FusionPaper.getSafeStopHandler().registerHandler(() -> {
            var currentState = engine.getInternals().getStateMachine().getCurrentState().getServerState();
            return currentState == DynamoServerState.LOBBY || currentState == DynamoServerState.LIMBO;
        });
        FusionPaper.getControlMessageHandler().registerHandler(ControlAction.MARK_STOPPING, () -> {
            if (engine.getInternals().getStateMachine().getCurrentState().getServerState() == DynamoServerState.LOBBY) {
                Bukkit.getScheduler().runTask(this, () ->
                        engine.getInternals().getStateMachine().changeState(new LimboState(this)));
            }
        });
    }

    @Override
    public void onDisable() {
        this.engine.getInternals().getStateMachine()
                .changeState(new LimboState(this));
    }

    private void beginHeartbeat() {
        Game<T, K> game = engine.getGame();
        FusionPaper.getServerInfo().ifPresentOrElse(
                serverInfo -> Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {

                    GameFrame<? extends GameState> frame = game.getGameFrame();
                    PlayerHandler playerHandler = game.getPlayerHandler();
                    List<TeamData> teamsData = game.getPlayerHandler().getTeams().stream()
                            .map(t -> new TeamData(t.getName(), t.getSize(), t.getRequiredPlayers()))
                            .toList();
                    FusionPaper.getServerDataStorage().storeInfo(new DynamoServerData(
                            serverInfo.getServerName(),
                            engine.getInternals().getStateMachine().getCurrentState().getServerState(),
                            frame.getGameType(),
                            new GameData(
                                    frame.getGamePlayType(),
                                    playerHandler.getTotalPlayerBounds(),
                                    teamsData
                            ),
                            Bukkit.getOnlinePlayers().size(),
                            UPDATE_ID.getAndIncrement()
                    ));
                }, 2 * 20, 5),
                () -> {
                    getLogger().severe("Server info not loaded yet, it should be! Shutting down...");
                    getPluginLoader().disablePlugin(this);
                }
        );
    }

    public static Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> getInstance() {
        return instance;
    }

    public static Engine<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> getEngine() {
        return instance.engine;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    void setEngine(Engine<T, K> engine) {
        this.engine = engine;
    }

    public CompletableFuture<Optional<String>> getNextServer() {
        return FusionPaper.getServerDataStorage().getAllServerData(DynamoServerData.class).thenApply(set -> {
            Optional<String> nextServer = Optional.empty();
            for (DynamoServerData dynamoServerData : set) {

                if (dynamoServerData.getServerState().isEmpty() ||
                        dynamoServerData.getPlayerCount().isEmpty() ||
                        dynamoServerData.getGameType().isEmpty()) {
                    continue;
                }

                DynamoServerState state = dynamoServerData.getServerState().get();
                int playerCount = dynamoServerData.getPlayerCount().get();
                GameType type = dynamoServerData.getGameType().get();
                if (state == DynamoServerState.LOBBY &&
                        playerCount == 0 &&
                        type == Dynamo.getEngine().getGame().getGameFrame().getGameType()) {
                    nextServer = Optional.of(dynamoServerData.getServerName());
                    break;
                }
            }
            return nextServer;
        });
    }
}
