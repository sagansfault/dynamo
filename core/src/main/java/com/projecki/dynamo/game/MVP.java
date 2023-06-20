package com.projecki.dynamo.game;

import org.bukkit.entity.Player;

public record MVP(Player player, Type type, String verb, String text, double value, boolean round) {

    public MVP(Player player, Type type, String verb, String text, double value) {
        this(player, type, verb, text, value, true);
    }

    public enum Type {
        HIGHEST("Highest"),
        LOWEST("Lowest"),
        LEAST("Least"),
        MOST("Most");

        private final String text;

        Type(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
