package com.projecki.dynamo;

import com.projecki.dynamo.game.Game;
import com.projecki.dynamo.game.PostGameInfo;
import com.projecki.dynamo.game.WorldHandler;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.state.DynamoGameState;
import com.projecki.dynamo.state.GameRunningState;
import com.projecki.dynamo.state.LimboState;
import com.projecki.dynamo.state.LobbyState;
import com.projecki.dynamo.state.PostGameState;
import com.projecki.fusion.game.state.GameState;
import com.projecki.fusion.game.state.GenericGameStateMachine;
import org.bukkit.scheduler.BukkitTask;

/**
 * The entry for your game into dynamo logic.
 */
public class Engine<T extends GameFrame<? extends GameState>, K extends PlayerHandler> {

    private final Dynamo<T, K> dynamo;
    private final Game<T, K> game;
    private final Internals internals;
    private final WorldHandler worldHandler;

    public Engine(Dynamo<T, K> dynamo, Game<T, K> game) {
        this.dynamo = dynamo;
        this.game = game;
        this.internals = new Internals(new GenericGameStateMachine<>(dynamo));
        worldHandler = new WorldHandler(dynamo, game.getGameOptions());
    }

    public Game<T, K> getGame() {
        return this.game;
    }

    /**
     * Gets Dynamo's internals. This is a container class to organize fields and objects that are specifically dynamo's
     * and should not be touched by anything other than dynamo.
     *
     * @return Dynamo's internals.
     */
    public Internals getInternals() {
        return this.internals;
    }

    public WorldHandler getWorldHandler() {
        return worldHandler;
    }

    /**
     * Ends your game. Your implementation should run this function when your game is over and should return players to
     * the lobby or to the hub. Use this function wisely
     *
     * Note, this function will only run if your game is actually running. If dynamo is in limbo or lobby state, this
     * function has no effect.
     */
    public void endGame(PostGameInfo postGameInfo) {
        // only proceed if the current state of dynamo is running
        GenericGameStateMachine<DynamoGameState> stateMachine = this.internals.getStateMachine();
        if (stateMachine.isCurrentState(GameRunningState.class)) {
            // stop the running game instance's state machine
            if (this.getGame().getGameFrame() == null) {
                stateMachine.changeState(new LimboState(this.dynamo));
            } else {
                stateMachine.complete();
                stateMachine.changeState(new PostGameState(this.dynamo, postGameInfo));
            }
        }
    }

    /**
     * Starts your game. This function will only have an effect if dynamo is in lobby state and will execute to the
     * first state as defined in your game frame. THIS FUNCTION DOES NOT ENSURE ANY OTHER PRE-REQUISITES ARE MET!
     */
    public void startGame() {
        GenericGameStateMachine<DynamoGameState> stateMachine = this.internals.getStateMachine();
        stateMachine.getCurrentStateAs(LobbyState.class).ifPresent(lobbyState -> {
            stateMachine.changeState(new GameRunningState(this.dynamo));
            lobbyState.getStartingTask().ifPresent(BukkitTask::cancel);
        });
    }
}
