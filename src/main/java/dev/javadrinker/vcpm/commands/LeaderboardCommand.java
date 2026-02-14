package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.StandardMessages;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardCommand extends ListenerAdapter {
    private static final int PAGE_SIZE = 10;

    // Tracks which page a message ID is showing
    private final Map<Long, Integer> messagePages = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("leaderboard")) return;

        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        ServerDataUtil.ServerData data = ServerDataUtil.getOrCreateServer(guild.getId());
        if (data.user_data == null || data.user_data.isEmpty()) {
            event.reply("No leaderboard data yet.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int initialPage = 0;
        EmbedBuilder embed = buildLeaderboardEmbed(guild, data, initialPage);

        event.replyEmbeds(embed.build())
                .setComponents(
                        ActionRow.of(
                                Button.secondary("leader_prev", "⬅ Prev").asDisabled(),
                                Button.secondary("leader_next", "Next ➡")
                        )
                )
                .queue(sentMessage -> {
                    long messageId = sentMessage.getIdLong();  // Correct way to get message ID
                    messagePages.put(messageId, initialPage);
                });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String customId = event.getComponentId();

        // Only handle our specific buttons
        if (!customId.equals("leader_prev") && !customId.equals("leader_next")) {
            return;
        }

        long messageId = event.getMessageIdLong();
        Integer currentPage = messagePages.getOrDefault(messageId, 0);

        Guild guild = event.getGuild();
        if (guild == null) {
            event.deferEdit().queue();
            return;
        }

        ServerDataUtil.ServerData data = ServerDataUtil.getOrCreateServer(guild.getId());
        List<Map.Entry<String, ServerDataUtil.UserData>> sorted =
                data.user_data.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue().score, a.getValue().score))
                        .collect(Collectors.toList());

        int totalPages = (sorted.size() - 1) / PAGE_SIZE;
        int newPage = currentPage;

        if (customId.equals("leader_next") && currentPage < totalPages) {
            newPage++;
        } else if (customId.equals("leader_prev") && currentPage > 0) {
            newPage--;
        }

        messagePages.put(messageId, newPage);

        EmbedBuilder newEmbed = buildLeaderboardEmbed(guild, data, newPage);

        // Build buttons with disabled states at edges
        Button prev = Button.secondary("leader_prev", "⬅ Prev");
        Button next = Button.secondary("leader_next", "Next ➡");

        if (newPage == 0) prev = prev.asDisabled();
        if (newPage >= totalPages) next = next.asDisabled();

        event.editMessageEmbeds(newEmbed.build())
                .setComponents(ActionRow.of(prev, next))
                .queue();
    }

    private EmbedBuilder buildLeaderboardEmbed(Guild guild,
                                               ServerDataUtil.ServerData data,
                                               int page) {
        List<Map.Entry<String, ServerDataUtil.UserData>> sorted =
                data.user_data.entrySet().stream()
                        .sorted((a, b) -> Integer.compare(b.getValue().score, a.getValue().score))
                        .collect(Collectors.toList());

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Prediction Leaderboard");

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sorted.size());

        StringBuilder desc = new StringBuilder();
        for (int i = start; i < end; i++) {
            Map.Entry<String, ServerDataUtil.UserData> entry = sorted.get(i);
            try {
                Member member = guild.retrieveMemberById(entry.getKey()).complete();
                desc.append(member.getAsMention());
            } catch (Exception ignored) {
                desc.append("<@").append(entry.getKey()).append(">\\*");
            }
            desc.append(" - ").append(entry.getValue().score).append("\n");
        }

        if (desc.isEmpty()) {
            desc.append("No entries on this page.");
        }

        embed.setDescription(desc.toString())
                .setFooter("Page " + (page + 1) + " • " + StandardMessages.randomRegularAdvert());

        if (!sorted.isEmpty()) {
            try {
                Member top = guild.retrieveMemberById(sorted.get(0).getKey()).complete();
                embed.setThumbnail(top.getEffectiveAvatarUrl());
            } catch (Exception ignored) {}
        }

        return embed;
    }
}
