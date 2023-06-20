package com.projecki.dynamo;

import com.projecki.fusion.game.GameType;
import com.projecki.fusion.server.BasicServerData;

import java.util.Optional;

public class DynamoServerData extends BasicServerData {

    private static final String SERVER_STATE = "dynamo_state";
    private static final String GAME_TYPE = "game_type";
    private static final String GAME_DATA = "game_data";
    private static final String UPDATE_ID = "update_id";
    public static final long DEFAULT_UPDATE_ID = -1;

    public DynamoServerData(String server) {
        super(server);
    }

    public DynamoServerData(String server,
                            DynamoServerState serverState,
                            GameType gameType,
                            GameData gameData,
                            int playerCount,
                            long updateId) {
        super(server, playerCount, System.currentTimeMillis());
        this.putField(SERVER_STATE, serverState);
        this.putField(GAME_TYPE, gameType);
        this.putField(GAME_DATA, gameData);
        this.putField(UPDATE_ID, updateId);
    }

    public Optional<DynamoServerState> getServerState() {
        return this.getField(SERVER_STATE, DynamoServerState.class);
    }

    public Optional<GameType> getGameType() {
        return this.getField(GAME_TYPE, GameType.class);
    }

    public Optional<GameData> getGameData() {
        return this.getField(GAME_DATA, GameData.class);
    }

    public long getUpdateId() {
        Optional<Long> id = this.getField(UPDATE_ID, long.class);
        // Prevent needless boxing
        //noinspection OptionalIsPresent
        return id.isPresent() ? id.get() : DEFAULT_UPDATE_ID;
    }
}
