package com.projecki.dynamo.state;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.DynamoServerState;
import com.projecki.dynamo.Messages;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.fusion.game.state.GameState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class LimboState extends DynamoGameState {
    
    public LimboState(Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo) {
        super(dynamo, DynamoServerState.LIMBO);
    }

    @Override
    public void onBegin() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.kick(Messages.SERVER_IN_LIMBO);
        }
    }

    @Override
    public void onFinish() {

    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onJoin(AsyncPlayerPreLoginEvent event) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.SERVER_IN_LIMBO);
    }
}
