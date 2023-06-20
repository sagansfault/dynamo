package com.projecki.dynamo.death.supplier;

import com.projecki.dynamo.death.DeathMessageSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Optional;

public class PlayerKilledByProjectileSupplier extends DeathMessageSupplier<EntityDamageByEntityEvent> {

    public PlayerKilledByProjectileSupplier() {
        super(EntityDamageByEntityEvent.class);
    }

    @Override
    public Optional<Component> get(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player victimPlayer && event.getDamager() instanceof Projectile projectile) {
            Component component = formatNameWithTeam(victimPlayer)
                    .append(Component.text(" was shot by ").color(NamedTextColor.GRAY));
            if (projectile.getShooter() instanceof Player damagingPlayer) {
                return Optional.of(component.append(formatNameWithTeam(damagingPlayer)));
            } else {
                return Optional.of(component.append(Component.text("a random projectile.").color(NamedTextColor.GRAY)));
            }
        } else {
            return Optional.empty();
        }
    }

}
