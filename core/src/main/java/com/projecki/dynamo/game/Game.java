package com.projecki.dynamo.game;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.death.DeathMessagePipeline;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.player.TeamPlayerHandler;
import com.projecki.fusion.game.state.GameState;

/**
 * Nothing much more than a container object for a game frame and a player handler.
 */
public class Game<T extends GameFrame<? extends GameState>, K extends PlayerHandler> {

    private final T gameFrame;
    private final K playerHandler;
    private final GameOptions gameOptions;
    private final MatchStats matchStats;
    private final DeathMessagePipeline deathMessagePipeline;

    public Game(T gameFrame, K playerHandler, GameOptions gameOptions) {
        this.gameFrame = gameFrame;

        this.playerHandler = playerHandler;
        if (playerHandler instanceof TeamPlayerHandler teamPlayerHandler) {
            teamPlayerHandler.initializeTeamData();
        }

        this.matchStats = new MatchStats();
        this.gameOptions = gameOptions;
        this.deathMessagePipeline = new DeathMessagePipeline(Dynamo.getInstance());
    }

    public Game(T gameFrame, K playerHandler) {
        this(gameFrame, playerHandler, new GameOptions());
    }

    public T getGameFrame() {
        return gameFrame;
    }

    public K getPlayerHandler() {
        return playerHandler;
    }

    public MatchStats getMatchStats() {
        return matchStats;
    }

    public GameOptions getGameOptions() {
        return gameOptions;
    }

    public DeathMessagePipeline getDeathMessagePipeline() {
        return deathMessagePipeline;
    }
}
