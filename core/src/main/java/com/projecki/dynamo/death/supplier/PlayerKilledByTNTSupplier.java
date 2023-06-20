package com.projecki.dynamo.death.supplier;

import com.projecki.dynamo.death.DeathMessageSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

public class PlayerKilledByTNTSupplier extends DeathMessageSupplier<EntityDamageByEntityEvent> {

    public PlayerKilledByTNTSupplier() {
        super(EntityDamageByEntityEvent.class);
    }

    @Override
    public Optional<Component> get(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victimPlayer && event.getDamager() instanceof TNTPrimed tntPrimed) {
            Component textComponent = formatNameWithTeam(victimPlayer)
                    .append(Component.text(" was blown up by ").color(NamedTextColor.GRAY));
            if (tntPrimed.getSource() instanceof Player damagingPlayer) {
                return Optional.of(textComponent.append(formatNameWithTeam(damagingPlayer)));
            } else {
                return Optional.of(textComponent.append(Component.text("TNT").color(NamedTextColor.GRAY)));
            }
        } else {
            return Optional.empty();
        }
    }

}
