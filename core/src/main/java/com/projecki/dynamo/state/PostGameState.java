package com.projecki.dynamo.state;

import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.DynamoServerState;
import com.projecki.dynamo.Engine;
import com.projecki.dynamo.ModelData;
import com.projecki.dynamo.RequeuePlayersMessage;
import com.projecki.dynamo.Util;
import com.projecki.dynamo.game.PostGameInfo;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.team.Team;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.button.AbstractButton;
import com.projecki.fusion.button.RegularButton;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.fusion.currency.PaperCurrency;
import com.projecki.fusion.customfloatingtexture.AbstractCustomFloatingTexture;
import com.projecki.fusion.customfloatingtexture.PacketBasedCustomFloatingTexture;
import com.projecki.fusion.customfloatingtexture.RegularCustomFloatingTexture;
import com.projecki.fusion.game.GameType;
import com.projecki.fusion.game.state.GameState;
import com.projecki.fusion.hologram.HologramAnchor;
import com.projecki.fusion.hologram.impl.Hologram;
import com.projecki.fusion.item.HotbarItem;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.map.config.ConfigSign;
import com.projecki.fusion.map.config.ConfigWorld;
import com.projecki.fusion.reward.Reward;
import com.projecki.fusion.reward.impl.CurrencyReward;
import com.projecki.fusion.util.ImmutableLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PostGameState extends DynamoGameState {

    private final PostGameInfo postGameInfo;
    private final List<Location> spawnLocations;
    private final ConfigWorld map;

    private final EntityManager entityManager = new EntityManager();

    public PostGameState(Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo, PostGameInfo postGameInfo) {
        super(dynamo, DynamoServerState.POST_GAME);
        this.postGameInfo = postGameInfo;

        Engine<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> engine = Dynamo.getEngine();

        map = engine.getWorldHandler().getSelectedLobby();
        spawnLocations = map.getSigns("post-game").stream()
                .map(ConfigSign::getLocation)
                .map(ImmutableLocation::mutable)
                .toList();

        engine.getGame().getMatchStats().saveToStatistics(FusionPaper.getStatisticLoader());
    }

    @Override
    public void onBegin() {
        Util.distributeRandom(Bukkit.getOnlinePlayers(), spawnLocations);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Util.cleanPlayer(player);
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.hidePlayer(dynamo, player);
            }
            player.sendMessage(postGameInfo.getStatsMessage());
            HotbarItem.give(player, 0, ItemBuilder.of(Material.COMPASS).name("Leave Game").build(), p -> {
                p.performCommand("hub");
            });
        }

        this.showLeaveGameButton();
        this.showWinLoseTextures();
        postGameInfo.getRewards().rowMap().forEach((player, map) -> {
            boolean played = false;
            for (Map.Entry<Reward, Component> entry : map.entrySet()) {
                Reward reward = entry.getKey();
                Component reason = entry.getValue();
                // only play the animation once
                if (!played && reward instanceof CurrencyReward currencyReward) {
                    this.playBitsAnimation(player, currencyReward);
                    played = true;
                }
                reward.reward(player);
                ComponentBuilder builder = ComponentBuilder.builder();
                if (reward instanceof CurrencyReward currencyReward) {
                    builder.content("+", NamedTextColor.GREEN)
                            .content(currencyReward.getCurrencyType().format(currencyReward.getReward()), ((PaperCurrency) currencyReward.getCurrencyType()).getPrimary());
                    if (reason != Component.empty()) {
                        builder.content(Component.space()).content(reason);
                    }
                }
                player.sendMessage(builder.toComponent());
            }
        });

        this.showSearchingForNextGameActionBar();
        this.startNextGameSearch();
    }

    @Override
    public void onFinish() {
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockY()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        event.setCancelled(true);
    }

    private void startNextGameSearch() {
        new BukkitRunnable() {
            int i = 15;
            @Override
            public void run() {
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                dynamo.getNextServer().thenAccept(serverOpt -> {
                    if (serverOpt.isPresent()) {
                        // entityManager.deleteAll(); Server restarts anyway, so these get removed anyway
                        String server = serverOpt.get();
                        Bukkit.getScheduler().runTask(dynamo, () -> {
                            CompletableFuture<Void> collective = CompletableFuture.allOf(
                                    players.stream()
                                            .map(player -> FusionPaper.getServerTransport().transport(player, server))
                                            .toArray(CompletableFuture[]::new)
                            );
                            collective.thenRun(Bukkit::shutdown);
                        });
                        this.cancel();
                    }
                });
                i--;
                if (i == 0) {
                    this.cancel();
                    for (Player player : players) {
                        player.performCommand("hub");
                    }
                    UUID[] playersArr = players.stream().map(Entity::getUniqueId).toArray(UUID[]::new);
                    GameType gameType = Dynamo.getEngine().getGame().getGameFrame().getGameType();
                    FusionPaper.getMessageClient().send(new RequeuePlayersMessage(gameType, playersArr));
                    Bukkit.shutdown();
                }
            }
        }.runTaskTimer(dynamo, 3 * 20, 20);
    }

    private void showSearchingForNextGameActionBar() {
        new BukkitRunnable() {
            int i = 15;
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(Component.text("Searching for next game " + i + "...", NamedTextColor.GREEN));
                }
                i--;
                if (i == 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(dynamo, 20, 20);
    }

    private void playBitsAnimation(Player player, CurrencyReward reward) {
        for (ConfigSign sign : map.getSigns("bits-reward")) {
            Location location = sign.getLocation();
            PacketBasedCustomFloatingTexture texture = new PacketBasedCustomFloatingTexture(location, 0, ModelData.getBits(0));

            HologramAnchor<Hologram> hologram = new HologramAnchor<>(new Hologram(Component.text("+ " + reward.getCurrencyType().format(reward.getReward()))));
            // hologram.render(location.clone().add(0, 1, 0), player);
            new BukkitRunnable() {
                int i = 0;
                @Override
                public void run() {
                    texture.updateCustomTexture(ModelData.getBits(i));
                    if (i == 15) {
                        this.cancel();
                        return;
                    }
                    i++;
                }
            }.runTaskTimer(dynamo, 20, 4);
            texture.show(player);

            entityManager.register(texture);
            entityManager.register(hologram);
        }
    }

    private void showLeaveGameButton() {
        for (ConfigSign sign : map.getSigns("leave-game")) {
            RegularCustomFloatingTexture texture = new RegularCustomFloatingTexture(sign.getLocation(), 0, ModelData.RETURN_BUTTON);
            RegularButton button = new RegularButton(dynamo, texture, 1.0f, 0.2f, 30);
            button.onEndHover(p -> button.getFloatingTexture().updateCustomTexture(ModelData.RETURN_BUTTON));
            button.onStartHover(p -> button.getFloatingTexture().updateCustomTexture(ModelData.RETURN_BUTTON_HOVER));
            button.onClick((p, a) -> p.performCommand("hub"));

            entityManager.register(texture);
            entityManager.register(button);
        }
    }

    private void showWinLoseTextures() {
        PlayerHandler playerHandler = Dynamo.getEngine().getGame().getPlayerHandler();
        Set<ConfigSign> signs = map.getSigns("victory-defeat");
        if (postGameInfo.getWinner().isEmpty()) {
            for (ConfigSign sign : signs) {
                RegularCustomFloatingTexture texture = new RegularCustomFloatingTexture(sign.getLocation(), 0, ModelData.DRAW);
                entityManager.register(texture);
            }
        } else {
            Team winner = postGameInfo.getWinner().get();
            for (ConfigSign sign : signs) {
                PacketBasedCustomFloatingTexture loserTexture = new PacketBasedCustomFloatingTexture(sign.getLocation(), 0, ModelData.DEFEAT);
                PacketBasedCustomFloatingTexture winnerTexture = new PacketBasedCustomFloatingTexture(sign.getLocation(), 0, ModelData.VICTORY);
                for (Team team : playerHandler.getTeams()) {
                    team.getPlayers().stream()
                            .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                            .forEach(p -> {
                                if (team.equals(winner)) {
                                    winnerTexture.show(p);
                                } else {
                                    loserTexture.show(p);
                                }
                            });
                }
                entityManager.register(loserTexture);
                entityManager.register(winnerTexture);
            }
        }
    }

    private final static class EntityManager {
        private final List<Hologram> holograms = new ArrayList<>();
        private final List<HologramAnchor<? extends Hologram>> anchors = new ArrayList<>();
        private final List<AbstractCustomFloatingTexture> textures = new ArrayList<>();
        private final List<AbstractButton> buttons = new ArrayList<>();

        public void register(Hologram hologram) {
            this.holograms.add(hologram);
        }

        public void register(HologramAnchor<? extends Hologram> anchor) {
            this.anchors.add(anchor);
        }

        public void register(AbstractCustomFloatingTexture texture) {
            this.textures.add(texture);
        }

        public void register(AbstractButton button) {
            this.buttons.add(button);
        }

        public void deleteAll() {
            holograms.forEach(Hologram::destroy);
            anchors.forEach(HologramAnchor::destroy);
            textures.forEach(AbstractCustomFloatingTexture::delete);
            buttons.forEach(AbstractButton::delete);
        }
    }
}
