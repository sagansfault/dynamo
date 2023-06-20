package com.projecki.dynamo;

import com.projecki.dynamo.state.DynamoGameState;
import com.projecki.fusion.game.state.GenericGameStateMachine;

public class Internals {

    private final GenericGameStateMachine<DynamoGameState> stateMachine;

    public Internals(GenericGameStateMachine<DynamoGameState> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public GenericGameStateMachine<DynamoGameState> getStateMachine() {
        return stateMachine;
    }
}
