package com.projecki.dynamo.state;

import com.google.common.collect.Sets;
import com.projecki.dynamo.Bounds;
import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.DynamoServerState;
import com.projecki.dynamo.Messages;
import com.projecki.dynamo.Util;
import com.projecki.dynamo.game.Game;
import com.projecki.dynamo.game.frame.GameFrame;
import com.projecki.dynamo.player.IndividualPlayerHandler;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.player.TeamPlayerHandler;
import com.projecki.dynamo.team.Team;
import com.projecki.dynamo.team.TeamColor;
import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.game.state.GameState;
import com.projecki.fusion.item.HotbarItem;
import com.projecki.fusion.item.ItemBuilder;
import com.projecki.fusion.map.config.ConfigSign;
import com.projecki.fusion.map.config.ConfigWorld;
import com.projecki.fusion.party.Party;
import com.projecki.fusion.party.member.Member;
import com.projecki.fusion.scoreboard.name.BelowName;
import com.projecki.fusion.scoreboard.name.PlayerName;
import com.projecki.fusion.scoreboard.tab.PlayerTabList;
import com.projecki.fusion.user.PaperUser;
import com.projecki.fusion.util.ImmutableLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;

public class LobbyState extends DynamoGameState {

    private final List<Location> lobbySpawnSigns;

    private @Nullable BukkitTask startingTask;
    private @Nullable BukkitTask playersNeededTask;
    private final Map<UUID, Team> cachedParties = new HashMap<>();

    // whether the game is starting or not
    private boolean starting;

    public LobbyState(Dynamo<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> dynamo) {
        super(dynamo, DynamoServerState.LOBBY);
        ConfigWorld map = Dynamo.getEngine().getWorldHandler().getSelectedLobby();
        this.lobbySpawnSigns = map.getSigns("spawn")
                .stream()
                .map(ConfigSign::getLocation)
                .map(ImmutableLocation::mutable)
                .toList();
    }

    @Override
    public void onBegin() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Util.cleanPlayer(player);
            this.intakePlayer(player);
        }
        Util.distributeRandom(Bukkit.getOnlinePlayers(), lobbySpawnSigns);

        playersNeededTask = getPlayersNeededTask();
    }

    @Override
    public void onFinish() {
        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        PlayerHandler playerHandler = game.getPlayerHandler();
        if (playerHandler instanceof IndividualPlayerHandler individual) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Team team = new Team(individual.generateTeamName(player), individual.generateTeamColor(player), Bounds.ONE);
                team.addPlayer(player.getUniqueId());
                playerHandler.putTeam(team);
            }
        } else {
            Set<Player> players = playerHandler.getTeams().stream()
                    .flatMap(team -> team.getPlayers().stream())
                    .flatMap(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).stream())
                    .collect(toSet());
            Set<Player> allOnline = new HashSet<>(Bukkit.getOnlinePlayers());
            for (Player player : Sets.difference(players, allOnline)) {
                Optional<Team> found = playerHandler.addToBestFitTeam(player.getUniqueId());
                if (found.isEmpty()) {
                    player.kick(Messages.SOMETHING_WRONG);
                }
            }
        }
        if (playersNeededTask != null) {
            playersNeededTask.cancel();
        }
    }

    @EventHandler
    public void quit(PlayerQuitEvent event) {
        PlayerHandler playerHandler = Dynamo.getEngine().getGame().getPlayerHandler();

        Party party = FusionPaper.getParties().getParty(event.getPlayer());
        for (Member member : party.members()) {

            playerHandler.removePlayer(member.id());
            Player player = Bukkit.getPlayer(member.id());
            if (player != null) {
                player.kick(Component.text("Your party left the server"));
            }
        }

        if (playerHandler instanceof IndividualPlayerHandler) {
            for (Member member : party.members()) {
                playerHandler.getTeam(member.id()).ifPresent(playerHandler::removeTeam);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.intakePlayer(event.getPlayer());
    }

    private void intakePlayer(Player player) {

        Util.cleanPlayer(player);
        teleportToSpawn(player);

        PaperUser user = FusionPaper.getUsers().get(player);

        // if enabled when not desired it will show an empty below name with a score of 0
        if (Dynamo.getEngine().getGame().getGameOptions().playerHealthBarAboveHeadFormat() != null) {
            user.enable(BelowName.class);
        }
        user.enable(PlayerName.class, PlayerTabList.class);

        HotbarItem.give(player, 8,
                ItemBuilder.of(Material.COMPASS).name("Leave Game").build(),
                p -> p.performCommand("hub"));

        UUID playerId = player.getUniqueId();
        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        PlayerHandler playerHandler = game.getPlayerHandler();
        if (playerHandler.getTotalAvailable() <= 0) {
            player.kick(Messages.LOBBY_GAME_FULL);
            return;
        }

        switch (game.getGameFrame().getGamePlayType()) {
            case INDIVIDUAL -> {
                Team team = new Team(player.getName(), TeamColor.RED, Bounds.ONE);
                team.addPlayer(playerId);
                playerHandler.putTeam(team);
            }
            case TEAM -> {

                if (!(playerHandler instanceof TeamPlayerHandler teamPlayerHandler)) {
                    throw new IllegalStateException("GamePlayType is TEAM but PlayerHandler is not TeamPlayerHandler");
                }

                Team cachedTeam = cachedParties.get(playerId);
                if (cachedTeam != null && cachedTeam.getAvailable() > 0) {
                    teamPlayerHandler.getCustomTargetTeam(player, cachedTeam).addPlayer(playerId);
                    this.attemptStartGame();
                    return;
                }

                boolean added = false;
                Party party = FusionPaper.getParties().getParty(player);
                List<Team> sorted = game.getPlayerHandler().getTeams().stream()
                        .sorted(comparingInt(Team::getAvailable).reversed()).toList(); // reversed i.e. most available first
                for (Team team : sorted) {

                    if (team.getAvailable() >= party.size()) {
                        added = true;
                        teamPlayerHandler.getCustomTargetTeam(player, team).addPlayer(playerId);
                        party.members().forEach(m -> cachedParties.put(m.id(), team));
                        break;
                    }
                }

                if (!added) {
                    // Run through all party members and add them to teams where
                    // space is available; filling the teams with the most space first
                    Iterator<Member> partyItr = party.members().iterator();
                    for (Team team : sorted) {

                        int available = team.getAvailable();
                        for (int i = 0; i < available && partyItr.hasNext(); i++) {

                            Member member = partyItr.next();
                            if (member.id().equals(playerId)) {
                                teamPlayerHandler.getCustomTargetTeam(player, team).addPlayer(playerId);
                            } else {
                                this.cachedParties.put(member.id(), team);
                            }
                        }

                        if (!partyItr.hasNext()) { // No more members to add
                            break;
                        }
                    }
                }
            }
        }

        this.attemptStartGame();
    }

    private void teleportToSpawn(Player player) {
        Util.distributeRandom(player, lobbySpawnSigns);
    }

    private void attemptStartGame() {
        if (this.canStartGame()) {
            this.startCountDown();
        }
    }

    private boolean canStartGame() {

        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        PlayerHandler playerHandler = game.getPlayerHandler();

        if (playerHandler instanceof TeamPlayerHandler teamPlayerHandler) {
            for (Team team : teamPlayerHandler.getTeams()) {
                if (team.getSize() < team.getRequiredPlayers().min()) {
                    return false;
                }
            }
        } else if (playerHandler instanceof IndividualPlayerHandler individualPlayerHandler) {
            return playerHandler.getTotalPlayers() >= individualPlayerHandler.getTotalPlayerBounds().min();
        }

        return true;
    }

    /**
     * Get the additional players that need to join for the game to start
     */
    private int getPlayersNeeded() {
        Game<? extends GameFrame<? extends GameState>, ? extends PlayerHandler> game = Dynamo.getEngine().getGame();
        return game.getPlayerHandler().getTotalPlayerBounds().min() - game.getPlayerHandler().getTotalPlayers();
    }

    private void startCountDown() {
        if (!starting) {
            if (playersNeededTask != null) {
                playersNeededTask.cancel();
            }

            starting = true;

            startingTask = new BukkitRunnable() {
                int i = 60;

                @Override
                public void run() {

                    if (!canStartGame()) {
                        this.cancel();
                        starting = false;
                        playersNeededTask = getPlayersNeededTask();
                        return;
                    }

                    if (i == 0) {
                        Dynamo.getEngine().startGame();
                    } else {
                        if (i > 5) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.sendActionBar(Component.text("Game Starting In " + i));
                            }
                        } else {
                            TextColor color = TextColor.fromHexString(switch (i) {
                                case 5 -> "#55E852";
                                case 4 -> "#AAF429";
                                case 3 -> "#FFFF00";
                                case 2 -> "#FF9E1E";
                                default -> "#FF3C3C"; // counts for 1 too
                            });
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                player.showTitle(Title.title(
                                        Component.text(i, color),
                                        Component.empty(),
                                        Title.Times.of(Ticks.duration(4), Ticks.duration(25), Ticks.duration(4)))
                                );
                            }
                        }
                        i--;
                    }
                }
            }.runTaskTimer(dynamo, 0, 20);
        }
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        event.setCancelled(true);

        if (event.getCause() == EntityDamageEvent.DamageCause.VOID && event.getEntity() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(dynamo, () -> teleportToSpawn(player), 1);
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setCancelled(true);
        Util.distributeRandom(event.getPlayer(), lobbySpawnSigns);
    }

    @EventHandler
    public void hunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    public Optional<BukkitTask> getStartingTask() {
        return Optional.ofNullable(this.startingTask);
    }

    private BukkitTask getPlayersNeededTask() {
        return Bukkit.getScheduler().runTaskTimer(dynamo, () -> {
            var needed = getPlayersNeeded();

            Bukkit.getOnlinePlayers().forEach(player ->
                    player.sendActionBar(
                            Component.text(needed)
                                    .color(TextColor.color(0x0cbff2))
                                    .append(Component.text(" player" + (needed > 1 ? "s" : "") + " needed", NamedTextColor.WHITE))
                    )
            );
        }, 0, 20);
    }
}
