package com.projecki.dynamo.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum TeamColor {

    BLACK(NamedTextColor.BLACK, DyeColor.BLACK, "", ""),
    WHITE(NamedTextColor.WHITE, DyeColor.WHITE, "", ""),
    BLUE(TextColor.fromHexString("#135FB6"), DyeColor.BLUE, "\uE117", "\uE131"),
    RED(TextColor.fromHexString("#D12020"), DyeColor.RED, "\uE110", "\uE124"),
    GREEN(TextColor.fromHexString("#1F811F"), DyeColor.GREEN, "\uE114", "\uE128"),
    LIME(TextColor.fromHexString("#6BD038"), DyeColor.LIME, "\uE113", "\uE127"),
    YELLOW(TextColor.fromHexString("#FFBB00"), DyeColor.YELLOW, "\uE112", "\uE126"),
    ORANGE(TextColor.fromHexString("#FF5100"), DyeColor.ORANGE, "\uE111", "\uE125"),
    PURPLE(TextColor.fromHexString("#A20EC3"), DyeColor.PURPLE, "\uE118", "\uE132"),
    PINK(TextColor.fromHexString("#FC4C97"), DyeColor.PINK, "\uE119", "\uE133"),
    CYAN(TextColor.fromHexString("#17BBD0"), DyeColor.CYAN, "\uE115", "\uE129"),
    AQUA(TextColor.fromHexString("#5CB4FF"), DyeColor.LIGHT_BLUE, "\uE116", "\uE130"),
    ;

    private final TextColor textColor;
    private final DyeColor dyeColor;
    private final String titleUnicode;
    private final String iconUnicode;

    TeamColor(TextColor textColor, DyeColor dyeColor, String titleUnicode, String iconUnicode) {
        this.textColor = textColor;
        this.dyeColor = dyeColor;
        this.titleUnicode = titleUnicode;
        this.iconUnicode = iconUnicode;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public DyeColor getDyeColor() {
        return dyeColor;
    }

    public Material getDyeColorToWool() {
        String base = this.dyeColor.name();
        String woolString = base + "_WOOL";
        Material wool = Material.RED_WOOL; // default value
        try {
            wool = Material.valueOf(woolString);
        } catch (IllegalArgumentException ignored) {}
        return wool;
    }

    public Component getTitleUnicode() {
        return Component.text(titleUnicode);
    }

    public Component getIconUnicode() {
        return Component.text(iconUnicode);
    }
}
