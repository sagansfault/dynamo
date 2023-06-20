package com.projecki.dynamo.death.supplier;

import com.projecki.dynamo.death.DeathMessageSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

public class UnknownDeathSupplier extends DeathMessageSupplier<EntityDamageEvent> {

    public UnknownDeathSupplier() {
        super(EntityDamageEvent.class);
    }

    @Override
    public Optional<Component> get(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victimPlayer) {
            return Optional.of(formatNameWithTeam(victimPlayer)
                    .append(Component.text(" was killed.").color(NamedTextColor.GRAY)));
        } else {
            return Optional.empty();
        }
    }

}
