package com.projecki.dynamo.death.supplier;

import com.projecki.dynamo.death.DeathMessageSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

public class PlayerKilledByPlayerSupplier extends DeathMessageSupplier<EntityDamageByEntityEvent> {

    public PlayerKilledByPlayerSupplier() {
        super(EntityDamageByEntityEvent.class);
    }

    @Override
    public Optional<Component> get(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player damagingPlayer && event.getEntity() instanceof Player victimPlayer) {
            return Optional.of(formatNameWithTeam(victimPlayer)
                    .append(Component.text(" has been slain by ").color(NamedTextColor.GRAY))
                    .append(formatNameWithTeam(damagingPlayer)));
        } else {
            return Optional.empty();
        }
    }

}
