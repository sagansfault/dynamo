package com.projecki.dynamo;

import com.projecki.fusion.game.GameType;
import com.projecki.fusion.message.MessageClient;

import java.util.UUID;

public class RequeuePlayersMessage implements MessageClient.Message {

    private GameType gameType;
    private UUID[] players;

    public RequeuePlayersMessage() {} // GSON, god I need to change to jackson

    public RequeuePlayersMessage(GameType gameType, UUID[] players) {
        this.gameType = gameType;
        this.players = players;
    }

    public GameType getGameType() {
        return gameType;
    }

    public UUID[] getPlayers() {
        return players;
    }
}
