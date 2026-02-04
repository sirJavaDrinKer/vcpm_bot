package dev.javadrinker.vcpm.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.requests.restaction.pagination.PollVotersPaginationAction;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor();

    public static void start(JDA jda) {
        SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                updateStatus(jda);

                for(Guild guild : jda.getGuilds()) {
                    gamesAnnounceCheck(guild);
                    checkForLiveGames(guild);
                    checkForPastGames(guild);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private static void updateStatus(JDA jda) {
        if (!MatchUtil.isLiveMatchAvailable(true)) {
            jda.getPresence().setActivity(Activity.watching("Waiting for the next match..."));
            return;
        }

        String statusText = null;
        String team1 = null;
        String team2 = null;
        String score1 = null;
        String score2 = null;
        String mapScore = null;
        try {
            List<MatchUtil.LiveMatch> matches = MatchUtil.getLiveMatches(true);
            team1=matches.get(0).team1;
            team2=matches.get(0).team2;

            team1=AbbreviationConverter.abbreviate(team1);
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

    private static void gamesAnnounceCheck(Guild guild) {
        List<MatchUtil.UpcomingMatch> upcoming;
        try {
            upcoming = MatchUtil.getUpcomingMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchUtil.UpcomingMatch matchSegment : upcoming) {
            boolean upcomingFound = UnixConversion.unixTimestampToMillis(matchSegment.unix_timestamp) - System.currentTimeMillis() < 12 * 60 * 60 * 1000;
            ServerDataUtil.ServerData serverDataUtil = ServerDataUtil.getOrCreateServer(guild.getId());

            if (!upcomingFound) {
                continue;
            }

            if (serverDataUtil.prediction_ids.containsKey(matchSegment.getMatchId())) {
                continue;
            }

            guild.getTextChannelById(serverDataUtil.prediction_channel).sendMessagePoll(
                    new MessagePollBuilder(matchSegment.team1 + " vs " + matchSegment.team2)
                            .addAnswer(matchSegment.team1)
                            .addAnswer(matchSegment.team2).build()
            ).queue(message -> {
                ServerDataUtil.setPredictionMessage(guild.getId(), matchSegment.getMatchId(), message.getId());
                ServerDataUtil.save();
            });
        }
    }

    private static void checkForLiveGames(Guild guild) {
        List<MatchUtil.LiveMatch> live;
        try {
            live = MatchUtil.getLiveMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchUtil.LiveMatch match:live){
            ServerDataUtil.ServerData serverDataUtil = ServerDataUtil.getOrCreateServer(guild.getId());

            if (!serverDataUtil.prediction_ids.containsKey(match.getMatchId())) {
                continue;
            }

            guild.getTextChannelById(serverDataUtil.prediction_channel)
                    .retrieveMessageById(serverDataUtil.prediction_ids.get(match.getMatchId()))
                    .queue(message -> {
                        if (!message.getPoll().isExpired()) {
                            guild.getTextChannelById(serverDataUtil.prediction_channel)
                                    .endPollById(serverDataUtil.prediction_ids.get(match.getMatchId())).queue();
                        }
                    });

        }
    }


    private static void checkForPastGames(Guild guild) {
        List<MatchUtil.PastMatch> past;
        try {
            past = MatchUtil.getPastMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchUtil.PastMatch match : past) {
            ServerDataUtil.ServerData data = ServerDataUtil.getOrCreateServer(guild.getId());
            String pollId = data.prediction_ids.get(match.getMatchId());

            if (pollId == null) continue;

            TextChannel channel = guild.getTextChannelById(data.prediction_channel);
            if (channel == null) continue;

            try {
                Message message = channel.retrieveMessageById(pollId).complete();
                MessagePoll poll = message.getPoll();

                if (poll != null) {
                    PollAwardUtil.awardFromPoll(
                            guild,
                            message,
                            getWinnerTeam(match)
                    );

                    System.out.println(getWinnerFlag(match));
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(getWinnerTeam(match)+" wins!");
                    embedBuilder.addField("Series details:", match.team1 + " (" + match.score1 + "-" + match.score2 + ") " + match.team2+
                            "\n["+match.tournament_name+"](https://vlr.gg"+match.match_page+")", false);
                    embedBuilder.setThumbnail(getWinnerFlag(match));
                    embedBuilder.setFooter("Check /leaderboard to see your points.");
                    message.replyEmbeds(embedBuilder.build()).queue();
                }

                ServerDataUtil.removePrediction(guild.getId(), match.getMatchId());
                ServerDataUtil.save();

            } catch (Exception e) {
                System.err.println("Failed to process match " + match.getMatchId() + ": " + e.getMessage());
            }
        }
    }
    private static String getWinnerTeam(MatchUtil.PastMatch match) {
        int score1 = Integer.parseInt(match.score1);
        int score2 = Integer.parseInt(match.score2);
        if (score1 > score2) {
            return match.team1;
        } else if (score2 > score1) {
            return match.team2;
        } else {
            return "Draw";
        }
    }
    private static String getWinnerFlag(MatchUtil.PastMatch match) throws IOException, InterruptedException {
        String team1 = match.team1;
        if (getWinnerTeam(match).equals(team1)) {
            return VLRTeamUtil.getTeamByName(match.team1).img;
        } else {
            return VLRTeamUtil.getTeamByName(match.team2).img;
        }
    }

}
