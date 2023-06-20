package com.projecki.dynamo;

import com.projecki.dynamo.game.Game;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.fusion.game.state.GameState;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * A java plugin child type that your game plugin should extend and fulfill.
 */
public abstract class DynamoGamePlugin<T extends GameFrame<? extends GameState>, K extends PlayerHandler> extends JavaPlugin {

    private Engine<T, K> engine;

    @Override
    public final void onLoad() {
        Dynamo<T, K> instance = (Dynamo<T, K>) Dynamo.getInstance();
        engine = new Engine<>(instance, generateGame());
        instance.setEngine(engine);
    }

    /**
     * This function is how Dynamo gets information about your game. This runs onLoad and should be fulfilled immediately.
     * That is, dependent async calls are not allowed and can have ramifications on the game data you send to dynamo.
     *
     * You should essentially construct a game object in this function and pass in your implementation of PlayerHandler
     * and GameFrame, returning it.
     *
     * @return The game prejudice data for dynamo to use
     */
    protected abstract Game<T, K> generateGame();

    public final Engine<T, K> getEngine() {
        return engine;
    }
}
