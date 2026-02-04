package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.AbbreviationConverter;
import dev.javadrinker.vcpm.util.MatchUtil;
import dev.javadrinker.vcpm.util.UnixConversion;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public class CheckUpcoming extends ListenerAdapter implements EventListener {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("upcoming")) {
            return;
        }

        List<MatchUtil.UpcomingMatch> upcoming;

        try {
            upcoming = MatchUtil.getUpcomingMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();

        embedBuilder.setTitle("Upcoming Matches");
        for(MatchUtil.UpcomingMatch match : upcoming) {
            String team1 = AbbreviationConverter.abbreviate(match.team1);
            String team2 = AbbreviationConverter.abbreviate(match.team2);

            long unixTime = UnixConversion.unixTimestampToMillis(match.unix_timestamp);
            unixTime = unixTime / 1000;


            embedBuilder.addField(
                    team1 + " vs " + team2,
                    "<t:"+ unixTime +":f> \n" +
                            "(<t:"+unixTime+":R>)",
                    true
            );
        }

        event.replyEmbeds(embedBuilder.build()).queue();

    }
}
