package com.projecki.dynamo;

/**
 * Simple enum mapping for the 3 states dynamo uses for ease of logic in DynamoServerData
 */
public enum DynamoServerState {
    LIMBO, LOBBY, GAME_RUNNING, POST_GAME
}
