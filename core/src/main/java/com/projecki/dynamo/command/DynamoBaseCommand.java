package com.projecki.dynamo.command;

import com.projecki.dynamo.Dynamo;
import com.projecki.fusion.command.base.PaperCommonBaseCommand;
import net.kyori.adventure.text.format.TextColor;

public class DynamoBaseCommand extends PaperCommonBaseCommand {

    public DynamoBaseCommand() {
        super(TextColor.color(0xFFAB99), TextColor.color(0xFF6C4D), Dynamo.getCommandManager());
    }
}
