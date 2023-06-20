package com.projecki.dynamo;

import java.util.List;

/**
 * @since May 01, 2022
 * @author Andavin
 */
public record GameData(GamePlayType gamePlayType,
                       Bounds requiredTeams,
                       List<TeamData> teamsData) {

    public GameData {
        teamsData = List.copyOf(teamsData);
    }
}
