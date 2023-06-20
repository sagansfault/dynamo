package com.projecki.dynamo.death;

import com.projecki.dynamo.death.supplier.PlayerKilledByBlockSupplier;
import com.projecki.dynamo.death.supplier.PlayerKilledByPlayerSupplier;
import com.projecki.dynamo.death.supplier.PlayerKilledByProjectileSupplier;
import com.projecki.dynamo.death.supplier.PlayerKilledByTNTSupplier;
import com.projecki.dynamo.death.supplier.UnknownDeathSupplier;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeathMessagePipeline implements Listener {

    private final JavaPlugin plugin;
    private final List<DeathMessageSupplier<? extends EntityDamageEvent>> pipeline;

    public DeathMessagePipeline(JavaPlugin plugin) {
        this.plugin = plugin;
        this.pipeline = new ArrayList<>();

        // add defaults
        this.pipeline.addAll(List.of(
                new PlayerKilledByBlockSupplier(),
                new PlayerKilledByPlayerSupplier(),
                new PlayerKilledByProjectileSupplier(),
                new PlayerKilledByTNTSupplier(),
                new UnknownDeathSupplier()
        ));
    }

    /**
     * Registers a given supplier at the front of the pipeline. This will have priority over all the other suppliers
     * in the pipeline at the time of adding.
     *
     * @param supplier The supplier to add to the front of the pipeline
     */
    public void addSupplier(DeathMessageSupplier<? extends EntityDamageEvent> supplier) {
        this.addSuppliers(supplier);
    }

    /**
     * Registers a given array of suppliers to the front of the pipeline. The whole array is inserted, in order, at the
     * front of the pipeline. This is NOT the same as calling {@link #addSupplier(DeathMessageSupplier)} repeatedly as
     * that will result in the array of suppliers being inserted in reverse order as each call adds the element to the
     * front of the pipeline.
     *
     * @param suppliers The suppliers to add
     */
    @SafeVarargs
    public final void addSuppliers(DeathMessageSupplier<? extends EntityDamageEvent>... suppliers) {
        for (int i = 0; i < suppliers.length; i++) {
            DeathMessageSupplier<? extends EntityDamageEvent> supplier = suppliers[i];
            pipeline.add(i, supplier);
        }
    }

    @EventHandler
    private void onDamage(PlayerDeathEvent event) {
        EntityDamageEvent damage = event.getPlayer().getLastDamageCause();
        if (damage == null) {
            return;
        }
        for (DeathMessageSupplier<? extends EntityDamageEvent> handler : pipeline) {
            Optional<Component> compOpt = handler.accept(damage);
            if (compOpt.isPresent()) {
                event.deathMessage(compOpt.get());
                return;
            }
        }
        event.deathMessage(null);
    }

    /**
     * Internal use only!
     * This is not called in the constructor because the constructor is called pre-onEnable and events can only be
     * registered in onEnable or later.
     */
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
