package com.projecki.dynamo.game.frame;

import com.projecki.dynamo.GamePlayType;
import com.projecki.fusion.game.GameType;
import com.projecki.fusion.game.state.GameState;
import com.projecki.fusion.game.state.GenericGameStateMachine;

/**
 * Represents a frame on which the game is constructed. No game logic should be present in this frame. This simply
 * contains data on the game itself for dynamo to use and read.
 *
 * For example, when some player requirement is met, dynamo needs to start your game. It does so by taking a reference
 * to your state machine and the first state you wish to start once the game starts. Dynamo then tells your state
 * machine to start this first state, starting your game logic.
 *
 * @param <T> This type is the super type of your game state machine parent state. For example if all my game states
 *           extended from a parent "CopsAndRobbersGameState" then that would be the type I pass in. If you do not have
 *           a parent state type then GameState will work.
 */
public class GameFrame<T extends GameState> {

    private final GenericGameStateMachine<T> stateMachine;
    private final T firstState;
    protected final GamePlayType gamePlayType;
    protected final GameType gameType;

    /**
     * Constructs a new game frame.
     *
     * @param stateMachine *Your* state machine controlling your game's logic
     * @param firstState The state you wish dynamo to switch to once the player requirements are met and your game
     *                   should start
     * @param gamePlayType The gameplay type
     * @param gameType The game type
     */
    public GameFrame(GenericGameStateMachine<T> stateMachine,
                     T firstState,
                     GamePlayType gamePlayType,
                     GameType gameType) {
        this.stateMachine = stateMachine;
        this.firstState = firstState;
        this.gamePlayType = gamePlayType;
        this.gameType = gameType;
    }

    /**
     * A function intended for internal use only. Use at your own risk!
     */
    public void changeToFirstState() {
        this.stateMachine.changeState(this.firstState);
    }

    /**
     * @return The state machine of *your* game you gave to this game-frame/dynamo when you constructed it
     */
    public GenericGameStateMachine<T> getStateMachine() {
        return stateMachine;
    }

    public T getFirstState() {
        return firstState;
    }

    public GamePlayType getGamePlayType() {
        return gamePlayType;
    }

    public GameType getGameType() {
        return gameType;
    }
}
