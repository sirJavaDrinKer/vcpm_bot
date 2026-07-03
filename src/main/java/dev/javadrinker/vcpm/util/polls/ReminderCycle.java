package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.data.MatchDataUtil;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import dev.javadrinker.vcpm.util.vlr.AbbreviationConverter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class ReminderCycle {
    public static void sendNewReminders(Guild guild) {
        if (!ServerDataUtil.getNewsEnabled(guild.getId())) { return; }

        List<MatchDataUtil.LiveMatch> live;
        try {
            live = MatchDataUtil.getLiveMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchDataUtil.LiveMatch match:live){
            ServerDataUtil.ServerData serverDataUtil = ServerDataUtil.getOrCreateServer(guild.getId());

            if (serverDataUtil.reminder_ids.containsKey(match.getMatchId())) {
                continue;
            }

            if(serverDataUtil.reminder_channel==(null)){
                continue;
            }

            TextChannel channel = guild.getTextChannelById(serverDataUtil.reminder_channel);

            if (channel == null) {
                continue;
            }

            if (match.team1.equals("TBD") || match.team2.equals("TBD")) {
                continue;
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(AbbreviationConverter.abbreviate(match.team1) + " vs " + AbbreviationConverter.abbreviate(match.team2));
            embedBuilder.setDescription(match.team1 +" vs "+match.team2+ " is now live, tune in!");
            embedBuilder.setColor(Color.GREEN);

            channel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
                ServerDataUtil.setReminderMessage(guild.getId(), match.getMatchId(), message.getId());
                ServerDataUtil.save();
            });
        }
    }
    public static void removeOldReminders(Guild guild){
        if (!ServerDataUtil.getNewsEnabled(guild.getId())) { return; }

        List<MatchDataUtil.PastMatch> past;
        try {
            past = MatchDataUtil.getPastMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchDataUtil.PastMatch match:past){
            ServerDataUtil.ServerData serverDataUtil = ServerDataUtil.getOrCreateServer(guild.getId());

            if (!serverDataUtil.reminder_ids.containsKey(match.getMatchId())) {
                continue;
            }

            serverDataUtil.reminder_ids.remove(match.getMatchId());
            ServerDataUtil.save();
        }
    }
}
