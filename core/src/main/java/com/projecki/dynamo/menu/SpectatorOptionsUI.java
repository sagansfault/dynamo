package com.projecki.dynamo.menu;

import com.projecki.dynamo.Dynamo;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.ui.inventory.GUI;
import com.projecki.fusion.ui.inventory.icon.Icon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class SpectatorOptionsUI extends GUI {

    public static final Map<UUID, Options> OPTIONS = new HashMap<>();

    private final JavaPlugin plugin;

    public SpectatorOptionsUI(JavaPlugin plugin) {
        super("Spectator Options", 9);
        this.plugin = plugin;
    }

    @Override
    protected void populate(Player player) {
        Options options = OPTIONS.getOrDefault(player.getUniqueId(), new Options());

        Icon nightVisionIcon = new Icon()
                .item(ItemBuilder.of(Material.INK_SAC).name(
                        ComponentBuilder.builder("Night Vision ").content(options.isNightVision() ? "[ON]" : "[OFF]", NamedTextColor.GRAY).toComponent()
                ).lore(Component.text("Toggle night vision", NamedTextColor.GRAY)).build())
                .sync()
                .action(click -> {
                    boolean nightVis = options.isNightVision();
                    options.setNightVision(!nightVis);
                    OPTIONS.put(player.getUniqueId(), options);
                    if (nightVis) {
                        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    } else {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 10000, 1, false, false, false));
                    }
                    this.populate(player);
                });

        Icon showOtherSpecsIcon = new Icon()
                .item(ItemBuilder.of(Material.SKELETON_SKULL).name(
                        ComponentBuilder.builder("Show Other Specs ").content(options.isShowOtherSpectators() ? "[ON]" : "[OFF]", NamedTextColor.GRAY).toComponent()
                ).lore(Component.text("Show other spectators", NamedTextColor.GRAY)).build())
                .sync()
                .action(click -> {
                    boolean showOtherSpecs = options.isShowOtherSpectators();
                    options.setShowOtherSpectators(!showOtherSpecs);
                    OPTIONS.put(player.getUniqueId(), options);
                    if (showOtherSpecs) {
                        for (UUID spectator : Dynamo.getEngine().getGame().getPlayerHandler().getSpectators()) {
                            Optional.ofNullable(Bukkit.getPlayer(spectator)).ifPresent(spec -> {
                                player.hidePlayer(plugin, spec);
                            });
                        }
                    } else {
                        for (UUID spectator : Dynamo.getEngine().getGame().getPlayerHandler().getSpectators()) {
                            Optional.ofNullable(Bukkit.getPlayer(spectator)).ifPresent(spec -> {
                                player.showPlayer(plugin, spec);
                            });
                        }
                    }
                    this.populate(player);
                });

        Icon speedIcon = new Icon().item(ItemBuilder.of(Material.LEATHER_BOOTS).name(
                ComponentBuilder.builder("Speed ").content("[" + options.getSpeed().getText() + "]", NamedTextColor.GRAY).toComponent()
                ).lore(Component.text("Toggle your walk speed", NamedTextColor.GRAY)).build())
                .sync()
                .action(click -> {
                    Options.Speed next = options.getSpeed().getNext();
                    next.apply(player);
                    options.setSpeed(next);
                    OPTIONS.put(player.getUniqueId(), options);
                    this.populate(player);
                });

        super.set(3, nightVisionIcon);
        super.set(5, showOtherSpecsIcon);
        super.set(7, speedIcon);
    }

    private static final class Options {

        private boolean nightVision = false;
        private boolean showOtherSpectators = false;
        private Speed speed;

        public boolean isNightVision() {
            return nightVision;
        }

        public void setNightVision(boolean nightVision) {
            this.nightVision = nightVision;
        }

        public boolean isShowOtherSpectators() {
            return showOtherSpectators;
        }

        public void setShowOtherSpectators(boolean showOtherSpectators) {
            this.showOtherSpectators = showOtherSpectators;
        }

        public Speed getSpeed() {
            return speed;
        }

        public void setSpeed(Speed speed) {
            this.speed = speed;
        }

        public enum Speed {
            CINEMATIC("Cinematic", player -> player.setWalkSpeed(0.3f)),
            NORMAL("Normal", player -> player.setWalkSpeed(1f)),
            FAST("Fast", player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 2, false, false, false))),
            FASTER("Faster" ,player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 3, false, false, false))),
            FASTEST("Fastest", player -> player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 4, false, false, false))),
            ;

            private final String text;
            private final Consumer<Player> applyFunction;

            Speed(String text, Consumer<Player> applyFunction) {
                this.text = text;
                this.applyFunction = applyFunction;
            }

            public String getText() {
                return text;
            }

            public void apply(Player player) {
                this.applyFunction.accept(player);
            }

            public Speed getNext() {
                int next = this.ordinal() + 1;
                if (next > Speed.values().length) {
                    next = 0;
                }
                return Speed.values()[next];
            }
        }
    }
}
