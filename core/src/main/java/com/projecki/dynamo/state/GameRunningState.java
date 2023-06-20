package com.projecki.dynamo.state;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.DynamoServerState;
import com.projecki.dynamo.Messages;
import com.projecki.dynamo.Util;
import com.projecki.dynamo.game.Game;
import com.projecki.dynamo.game.GameOptions;
import com.projecki.dynamo.game.GameSession;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.menu.SpectatorOptionsUI;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.player.TeamPlayerHandler;
import com.projecki.dynamo.team.Team;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.fusion.currency.Currency;
import com.projecki.fusion.currency.CurrencyRegister;
import com.projecki.fusion.game.state.GameState;
import com.projecki.fusion.scoreboard.name.BelowName;
import com.projecki.fusion.scoreboard.name.PlayerName;
import com.projecki.fusion.scoreboard.tab.PlayerTabList;
import com.projecki.fusion.user.PaperUser;
import com.projecki.fusion.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;

public class GameRunningState extends DynamoGameState {

    private final GameSession session;
    private BukkitTask actionBarTask;

    public GameRunningState(Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo) {
        super(dynamo, DynamoServerState.GAME_RUNNING);
        this.session = new GameSession();
    }

    @Override
    public void onBegin() {
        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        GameOptions.TeamOptions teamOptions = game.getGameOptions().teamOptions();
        PlayerHandler playerHandler = game.getPlayerHandler();

        // run before their first game state starts
        Bukkit.getOnlinePlayers().forEach(Util::cleanPlayer);

        this.session.start();
        game.getGameFrame().changeToFirstState();

        // define the player info list for each player's tab list and set other fields
        for (Team team : playerHandler.getTeams()) {
            for (UUID uuid : team.getPlayers()) {
                FusionPaper.getUsers().get(uuid).ifPresent(user -> {

                    Component unicode = team.getColorIconUnicodeOverride().orElse(team.getColor().getIconUnicode());
                    ComponentBuilder builder = ComponentBuilder.builder();
                    if (teamOptions.showTabUnicode()) {
                        builder.content(unicode).content(space());
                    }

                    if (teamOptions.teamColorInTab()) {
                        builder.content(user.name(), team.getColor().getTextColor());
                    } else {
                        builder.content(user.name());
                    }

                    Component name = builder.toComponent();
                    user.get(PlayerTabList.class).modify((t, i) -> i.modifyDisplayName(__ -> name));

                    PlayerName playerName = user.get(PlayerName.class);
                    playerName.update(unicode.append(space()), empty());
                });
            }
        }

        // set the health above the player's head
        Function<Player, Pair<Integer, Component>> format =
                Dynamo.getEngine().getGame().getGameOptions().playerHealthBarAboveHeadFormat();
        if (format != null) {
            FusionPaper.getUsers().getOnline().forEach(u -> this.updateHealthBarAboveHead(u, format));
        }

        this.toggleTeammateGlow(true);
        // send actionbar team unicode titles
        if (teamOptions.showActionBarUnicode()) {
            actionBarTask = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        playerHandler.getTeam(player.getUniqueId()).ifPresent(team -> {
                            Component unicode = team.getColorTitleUnicodeOverride().orElse(team.getColor().getTitleUnicode());
                            player.sendActionBar(text("\uf82a\uf82a\uf82a\uf823\uf832").append(unicode));
                        });
                    }
                }
            }.runTaskTimer(dynamo, 0, 19);
        }
    }

    @Override
    public void onFinish() {
        this.session.stop();
        SpectatorOptionsUI.OPTIONS.clear();
        this.toggleTeammateGlow(false);

        if (actionBarTask != null) {
            actionBarTask.cancel();
        }
    }

    @EventHandler
    public void heal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player player) {
            updateHealthBarAboveHead(player);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            updateHealthBarAboveHead(player);
        }
    }

    private void sendTablistHeaderFooter(Player player) {
        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        Optional<Currency> bitsTypeOpt = CurrencyRegister.getCurrency("bits");
        long value = bitsTypeOpt.map(currency -> FusionPaper.getPlayerCurrencyCache().getCachedBalance(currency, player.getUniqueId()).orElse(0L))
                .orElse(0L);
        player.sendPlayerListHeaderAndFooter(
                ComponentBuilder.builder()
                        .newLine()
                        .newLine()
                        .content("Gridcraft Network", TextColor.fromHexString("#0cbff2"), TextDecoration.BOLD)
                        .newLine()
                        .content(game.getGameFrame().getGameType().getDisplayName(), NamedTextColor.GRAY)
                        .toComponent(),
                ComponentBuilder.builder()
                        .newLine()
                        .content("Discord: ", TextColor.fromHexString("#0cbff2")).content("discord.gg/gridcraft")
                        .newLine()
                        .content("IP: ", TextColor.fromHexString("#0cbff2")).content("play.gridcraft.net")
                        .newLine()
                        .toComponent()
        );
    }

    private void updateHealthBarAboveHead(Player player) {

        Function<Player, Pair<Integer, Component>> format =
                Dynamo.getEngine().getGame().getGameOptions().playerHealthBarAboveHeadFormat();
        if (format != null) {
            PaperUser user = FusionPaper.getUsers().get(player);
            this.updateHealthBarAboveHead(user, format);
        }
    }

    private void updateHealthBarAboveHead(PaperUser user, Function<Player, Pair<Integer, Component>> format) {

        BelowName belowName = user.get(BelowName.class);
        Pair<Integer, Component> applied = format.apply(user.reference());

        int score = applied.fst;
        Component text = applied.snd;

        belowName.score(score);
        belowName.displayName(text);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.kick(Messages.GAME_ALREADY_RUNNING);
    }

    @EventHandler(ignoreCancelled = true)
    public void leave(PlayerQuitEvent event) {
        Dynamo.getEngine().getGame().getPlayerHandler().removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (Dynamo.getEngine().getGame().getPlayerHandler().isSpectator(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void pickUp(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (Dynamo.getEngine().getGame().getPlayerHandler().isSpectator(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (Dynamo.getEngine().getGame().getPlayerHandler().isSpectator(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    private PacketContainer getGlowingPacket(Entity targetToGlow, boolean glow) {
        PacketContainer glowPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        glowPacket.getIntegers().write(0, targetToGlow.getEntityId());

        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setEntity(targetToGlow);
        if (glow) {
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
            watcher.setObject(0, serializer, (byte) (0x40));
        }

        glowPacket.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());

        return glowPacket;
    }

    private void toggleTeammateGlow(boolean glow) {
        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        PlayerHandler playerHandler = game.getPlayerHandler();
        if (game.getGameOptions().glowingTeammates()) {
            if (playerHandler instanceof TeamPlayerHandler teamPlayerHandler) {
                for (Team team : teamPlayerHandler.getTeams()) {
                    List<Player> playersCasted = team.getPlayers().stream()
                            .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream()).toList();
                    for (Player single : playersCasted) {
                        for (Player other : playersCasted) {
                            if (!other.getUniqueId().equals(single.getUniqueId())) {
                                try {
                                    ProtocolLibrary.getProtocolManager().sendServerPacket(single, this.getGlowingPacket(other, glow));
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void playDeathAnimation(LivingEntity entity) {
        int id = entity.getEntityId();
        PacketContainer status = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
        status.getIntegers().write(0, id);
        status.getBytes().write(0, ((byte) 3));
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, status);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
