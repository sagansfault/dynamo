package com.projecki.dynamo.game;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.projecki.dynamo.Dynamo;
import com.projecki.dynamo.player.IndividualPlayerHandler;
import com.projecki.dynamo.player.PlayerHandler;
import com.projecki.dynamo.player.TeamPlayerHandler;
import com.projecki.dynamo.team.Team;
import com.projecki.fusion.component.ComponentBuilder;
import com.projecki.fusion.reward.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PostGameInfo {

    private final Set<MVP> mvps;
    private final @Nullable Team first;
    private final @Nullable Team second;
    private final @Nullable Team third;
    private final Table<Player, Reward, Component> rewards;
    private final Component statsMessage;

    private PostGameInfo(@Nullable Team first, @Nullable Team second, @Nullable Team third, Set<MVP> mvps, Table<Player, Reward, Component> rewards) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.mvps = mvps;
        this.rewards = rewards;

        statsMessage = this.generateMessage();
    }

    private Component generateMessage() {
        ComponentBuilder builder = ComponentBuilder.builder();
        builder.content("                                                            ", NamedTextColor.YELLOW, TextDecoration.STRIKETHROUGH);
        builder.newLine();

        if (first == null) {
            builder.content("      It's a Draw!", TextDecoration.BOLD).newLine().newLine();
        } else {
            PlayerHandler playerHandler = Dynamo.getEngine().getGame().getPlayerHandler();
            boolean secondAndThirdNotNull = second != null && third != null;

            if (playerHandler instanceof IndividualPlayerHandler) {
                builder.content(first.getName(), first.getColor().getTextColor(), TextDecoration.BOLD)
                        .content(" has won the game!", TextDecoration.BOLD);
            } else if (playerHandler instanceof TeamPlayerHandler) {
                builder.content("      Team ", TextDecoration.BOLD).content(first.getName(), first.getColor().getTextColor(), TextDecoration.BOLD)
                        .content(" has won the game!", TextDecoration.BOLD).newLine().newLine();
            } else {
                throw new IllegalStateException("Unsupported PlayerHandler type");
            }

            if (secondAndThirdNotNull) {
                builder.newLine().content("2nd", NamedTextColor.GRAY).content(second.getName())
                        .newLine().content("3rd", TextColor.fromHexString("#cd7f32")).content(third.getName());
            }
            builder.newLine().content("Match MVPs", NamedTextColor.YELLOW).newLine();
            for (MVP mvp : mvps) {

                builder.content(mvp.type().getText()).content(" " + mvp.verb() + " ");

                Player player = mvp.player();
                Optional<Team> teamOpt = playerHandler.getTeam(player.getUniqueId());
                if (teamOpt.isPresent()) {
                    Team team = teamOpt.get();
                    Component unicode = team.getColorIconUnicodeOverride().orElse(team.getColor().getIconUnicode());
                    builder.content(unicode).content(player.getName(), team.getColor().getTextColor());
                } else {
                    builder.content(mvp.player().getName(), NamedTextColor.GOLD);
                }

                if (mvp.round()) {
                    int rounded = (int) Math.round(mvp.value());
                    builder.content(" (" + rounded + " " + mvp.text() + ")");
                } else {
                    double rounded = Math.round(mvp.value() * 100.0) / 100.0;
                    builder.content(" (" + rounded + " " + mvp.text() + ")");
                }
                builder.newLine();
            }
        }

        builder.content("                                                            ", NamedTextColor.YELLOW, TextDecoration.STRIKETHROUGH);
        return builder.toComponent();
    }

    public Component getStatsMessage() {
        return statsMessage;
    }

    public Table<Player, Reward, Component> getRewards() {
        return rewards;
    }

    public Optional<Team> getWinner() {
        return Optional.ofNullable(this.first);
    }

    public static final class Builder {

        private @Nullable Team first = null;
        private @Nullable Team second = null;
        private @Nullable Team third = null;
        private final Table<Player, Reward, Component> rewards = HashBasedTable.create();
        private final Set<MVP> mvps = new HashSet<>();

        public Builder first(Team team) {
            this.first = team;
            return this;
        }

        public Builder second(Team team) {
            this.second = team;
            return this;
        }

        public Builder third(Team team) {
            this.third = team;
            return this;
        }

        public Builder putReward(Player player, Reward reward) {
            return this.putReward(player, reward, Component.empty());
        }

        public Builder putReward(Player player, Reward reward, Component reason) {
            this.rewards.put(player, reward, reason);
            return this;
        }

        public Builder addMvp(MVP mvp) {
            mvps.add(mvp);
            return this;
        }

        public PostGameInfo build() {
            return new PostGameInfo(first, second, third, mvps, rewards);
        }
    }
}
