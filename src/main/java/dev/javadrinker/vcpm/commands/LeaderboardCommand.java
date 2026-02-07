package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.StandardMessages;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.Map;

public class LeaderboardCommand extends ListenerAdapter {

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

        ServerDataUtil.ServerData data =
                ServerDataUtil.getOrCreateServer(guild.getId());

        if (data.user_data == null || data.user_data.isEmpty()) {
            event.reply("No leaderboard data yet")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Sort users by score DESC
        List<Map.Entry<String, ServerDataUtil.UserData>> sorted =
                data.user_data.entrySet()
                        .stream()
                        .sorted((a, b) ->
                                Integer.compare(b.getValue().score, a.getValue().score))
                        .limit(10) // top 10
                        .toList();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Prediction Leaderboard");

        int rank = 1;
        StringBuilder description = new StringBuilder();
        for (Map.Entry<String, ServerDataUtil.UserData> entry : sorted) {
            String userId = entry.getKey();
            int score = entry.getValue().score;

            Member member = guild.retrieveMemberById(userId).complete();
            String name = member.getAsMention();

            description.append(name).append(" - ").append(score).append("\n");

            rank++;
        }

        embed.setDescription(description.toString());

        embed.setFooter(StandardMessages.randomRegularAdvert());

        Member top = guild.retrieveMemberById(sorted.get(0).getKey()).complete();
        embed.setThumbnail(top.getEffectiveAvatarUrl());

        event.replyEmbeds(embed.build()).setSuppressedNotifications(true).queue();
    }
}
