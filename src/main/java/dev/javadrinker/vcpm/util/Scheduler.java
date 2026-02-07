package dev.javadrinker.vcpm.util;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import dev.javadrinker.vcpm.util.vlr.AbbreviationConverter;
import dev.javadrinker.vcpm.util.data.MatchDataUtil;
import dev.javadrinker.vcpm.util.data.TeamDataUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dev.javadrinker.vcpm.util.polls.PredictionCycle.*;

public class Scheduler {

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor();

    public static void start(JDA jda) {
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                if (!TeamDataUtil.isFileCacheEmpty()) {
                    updateStatus(jda);
                    for(Guild guild : jda.getGuilds()) {
                        if (!ServerDataUtil.getPredictionsEnabled(guild.getId())) {
                            continue;
                        }

                        gamesAnnounceCheck(guild);
                        checkForLiveGames(guild);
                        checkForPastGames(guild);
                    }
                } else {
                    jda.getPresence().setActivity(Activity.watching("Loading teams after restart..."));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private static void updateStatus(JDA jda) {
        if (!MatchDataUtil.isLiveMatchAvailable(true)) {
            jda.getPresence().setActivity(Activity.watching(StandardMessages.randomEasterEgg()));
            return;
        }

        String statusText = null;
        String team1 = null;
        String team2 = null;
        String score1 = null;
        String score2 = null;
        String mapScore = null;
        try {
            List<MatchDataUtil.LiveMatch> matches = MatchDataUtil.getLiveMatches(true);
            team1=matches.get(0).team1;
            team2=matches.get(0).team2;

            team1= AbbreviationConverter.abbreviate(team1);
            team2=AbbreviationConverter.abbreviate(team2);

            score1 = matches.get(0).score1;
            score2 = matches.get(0).score2;

            mapScore = " ("+matches.get(0).getTeam1MapScore()+")-("+matches.get(0).getTeam2MapScore()+") ";

            statusText = team1 + " " +score1 + " : " + score2 + " " + team2 + " | " + mapScore;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        jda.getPresence().setActivity(
                Activity.watching(statusText)
        );
    }
}
