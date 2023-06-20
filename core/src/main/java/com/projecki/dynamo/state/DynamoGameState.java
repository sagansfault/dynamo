package com.projecki.dynamo.state;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.DynamoServerState;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.fusion.game.state.GameState;

/**
 * Dynamo's internal state machine, not for plugin use.
 */
public abstract class DynamoGameState extends GameState {

    protected final Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo;
    protected final DynamoServerState serverState;

    public DynamoGameState(Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo, DynamoServerState serverState) {
        this.dynamo = dynamo;
        this.serverState = serverState;
    }

    public DynamoServerState getServerState() {
        return serverState;
    }

    public Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> getDynamo() {
        return dynamo;
    }
}
