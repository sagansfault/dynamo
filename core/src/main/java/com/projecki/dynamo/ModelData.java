package com.projecki.dynamo;

public final class ModelData {

    public static final int RETURN_BUTTON = 80;
    public static final int RETURN_BUTTON_HOVER = 81;
    public static final int VICTORY = 82;
    public static final int DEFEAT = 58;
    public static final int DRAW = 59;

    public static int getPlayAgainButton(int time) {
        return 60 + Math.max(0, Math.min(9, time));
    }

    public static int getPlayAgainButtonHover(int time) {
        return 70 + Math.max(0, Math.min(9, time));
    }

    public static int getBits(int amount) {
        return 83 + Math.max(0, Math.min(15, amount));
    }
}
