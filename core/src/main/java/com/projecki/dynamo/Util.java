package com.projecki.dynamo;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Util {

    private static final Random RANDOM = ThreadLocalRandom.current();

    public static void cleanPlayer(Player player) {
        player.getInventory().clear();
        player.closeInventory();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.setCollidable(true);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.setWalkSpeed(0.2f);
        player.setGameMode(Dynamo.getEngine().getGame().getGameOptions().gameMode());
        player.setSaturation(100);
        player.setHealth(20);
        player.setFoodLevel(20);
    }

    public static void distributeRandom(Collection<? extends Player> players, Collection<Location> locations) {
        if (locations.isEmpty()) {
            System.out.println("Distribute random called with empty locations collection");
            return;
        }

        Location[] arr = locations.toArray(Location[]::new);
        for (Player player : players) {
            player.teleportAsync(arr[RANDOM.nextInt(arr.length)]);
        }
    }

    public static void distributeRandom(Player player, Collection<Location> locations) {
        distributeRandom(Collections.singleton(player), locations);
    }
}
