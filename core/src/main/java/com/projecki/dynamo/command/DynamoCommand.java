package com.projecki.dynamo.command;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.state.LobbyState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@CommandAlias("dynamo")
@CommandPermission("dynamo.admin")
public class DynamoCommand extends DynamoBaseCommand {

    @Subcommand("start")
    @Description("Force the game to start while in lobby")
    public void forceStart(Player player) {
        var currentState = Dynamo.getEngine().getInternals().getStateMachine().getCurrentStateAs(LobbyState.class);

        if (currentState.isPresent()) {
            player.sendMessage(prefix.append(Component.text("Starting the game...", primaryColor)));
            Dynamo.getEngine().startGame();
        } else {
            player.sendMessage(prefix.append(Component.text("This command requires the server is in lobby", primaryColor)));
        }
    }
}
