package com.projecki.dynamo.menu;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.team.Team;
import com.projecki.fusion.ui.inventory.PageableGUI;
import com.projecki.fusion.ui.inventory.icon.Icon;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class SpectatorUI extends PageableGUI<UUID> {

    private static final Icon FILLER = Icon.of(Material.GRAY_STAINED_GLASS_PANE)
            .name(empty()).buildIcon();
    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final JavaPlugin plugin;

    public SpectatorUI(JavaPlugin plugin) {
        super("Player Selector", 54, 0, 8, SLOTS.length);
        this.outline(0, 53, FILLER);

        this.plugin = plugin;
    }

    @Override
    protected Icon blankBackSlotIcon() {
        return FILLER;
    }

    @Override
    protected Icon blankNextSlotIcon() {
        return FILLER;
    }

    @Override
    protected List<UUID> getItems(Player player) {
        PlayerHandler playerHandler = Dynamo.getEngine().getGame().getPlayerHandler();
        return playerHandler.getTeams().stream()
                .map(Team::getPlayers)
                .flatMap(Set::stream)
                .filter(uuid -> !playerHandler.isSpectator(uuid))
                .toList();
    }

    @Override
    protected void populate(Player player, List<UUID> items, int start, int end) {

        for (int i = start, slot = 0; i < end; i++, slot++) {
            UUID playerId = items.get(i);
            Player online = Bukkit.getPlayer(playerId);
            this.set(SLOTS[slot], Icon.of(Bukkit.createProfile(items.get(i)))
                    .name(online != null ? text(online.getName(), GREEN) : text("Offline", RED))
                    .buildIcon()
                    .action(click -> {

                        Player p = Bukkit.getPlayer(playerId);
                        if (p != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> {
                                click.player().teleport(p);
                                click.player().closeInventory();
                                click.playSound(Sound.ENTITY_ENDERMAN_TELEPORT);
                            });
                        } else {
                            click.error("Player Offline");
                            click.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0);
                        }
                    }));
        }
    }
}
