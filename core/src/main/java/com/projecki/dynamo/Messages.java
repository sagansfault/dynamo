package com.projecki.dynamo;

import com.projecki.fusion.component.ComponentBuilder;
import net.kyori.adventure.text.Component;

public class Messages {

    public static final Component SERVER_IN_LIMBO = ComponentBuilder.builder("Sorry, this server is in limbo").toComponent();
    public static final Component GAME_ALREADY_RUNNING = ComponentBuilder.builder("This game is already running, try another").toComponent();
    public static final Component WORLD_NOT_LOADED_PROPERLY = ComponentBuilder.builder("Sorry, our world hasn't loaded properly.")
            .newLine().content("Message a staff member!").toComponent();
    public static final Component LOBBY_GAME_FULL = ComponentBuilder.builder("Sorry, this game is full, try another.").toComponent();
    public static final Component SOMETHING_WRONG = ComponentBuilder.builder("Something went wrong, try another game.").toComponent();
}
