package com.projecki.dynamo.death.supplier;

import com.projecki.dynamo.death.DeathMessageSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

import java.util.Optional;

public class PlayerKilledByBlockSupplier extends DeathMessageSupplier<EntityDamageByBlockEvent> {

    public PlayerKilledByBlockSupplier() {
        super(EntityDamageByBlockEvent.class);
    }

    @Override
    public Optional<Component> get(EntityDamageByBlockEvent event) {
        if (event.getEntity() instanceof Player victimPlayer) {
            return Optional.of(formatNameWithTeam(victimPlayer)
                    .append(Component.text(" died trying to fight the world.").color(NamedTextColor.GRAY)));
        } else {
            return Optional.empty();
        }
    }

}
