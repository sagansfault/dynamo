package com.projecki.dynamo;

/**
 * @since May 01, 2022
 * @author Andavin
 */
public record Bounds(int min, int max) {

    /**
     * A {@link Bounds} with {@code 1} as the min and max.
     */
    public static final Bounds ONE = new Bounds(1, 1);

    public Bounds {
        // Ensure min and max are correct
        int realMin = Math.min(min, max);
        int realMax = Math.max(min, max);
        min = realMin;
        max = realMax;
    }

    /**
     * Returns the {@link Bounds} with the least values
     * (i.e. which one is less than the other).
     *
     * @param o The {@link Bounds} to compare to.
     * @return The minimum {@link Bounds}.
     */
    public Bounds min(Bounds o) {

        if (max < o.max) { // this < o
            return this;
        }

        if (max == o.max) { // this == o
            return min < o.min ? this : o; // this < o : this : o
        }

        return o; // o <= this
    }
}
