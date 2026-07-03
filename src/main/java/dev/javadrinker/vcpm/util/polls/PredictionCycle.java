package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.StandardMessages;
import dev.javadrinker.vcpm.util.UnixConversion;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import dev.javadrinker.vcpm.util.data.MatchDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static dev.javadrinker.vcpm.util.polls.WinnerUtil.getWinnerFlag;
import static dev.javadrinker.vcpm.util.polls.WinnerUtil.getWinnerTeam;

public class PredictionCycle {
    public static void gamesAnnounceCheck(Guild guild) {
        System.out.println("Checking for upcoming matches in guild: " + guild.getName());
        List<MatchDataUtil.UpcomingMatch> upcoming;
        try {
            System.out.println("Fetching upcoming matches...");
            upcoming = MatchDataUtil.getUpcomingMatches(true);
            System.out.println("Fetched " + upcoming.size() + " upcoming matches.");
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to fetch upcoming matches: " + e.getMessage());
            throw new RuntimeException(e);
        }

        for (MatchDataUtil.UpcomingMatch matchSegment : upcoming) {
            System.out.println("Processing match: " + matchSegment.getMatchId() + " - " + matchSegment.team1 + " vs " + matchSegment.team2);

            if (matchSegment.team1.equals("TBD") || matchSegment.team2.equals("TBD")) {
                System.out.println("Match " + matchSegment.getMatchId() + " has TBD teams. Skipping.");
                continue;
            }

            boolean upcomingFound = UnixConversion.unixTimestampToMillis(matchSegment.unix_timestamp) - System.currentTimeMillis() < 12 * 60 * 60 * 1000;
            ServerDataUtil.ServerData serverDataUtil = ServerDataUtil.getOrCreateServer(guild.getId());

            if (!upcomingFound) {
                System.out.println("Match " + matchSegment.getMatchId() + " is not within the next 12 hours. Skipping.");
                continue;
            }

            if (serverDataUtil.prediction_ids.containsKey(matchSegment.getMatchId())) {
                System.out.println("Prediction for match " + matchSegment.getMatchId() + " already exists. Skipping.");
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

    public static void checkForLiveGames(Guild guild) {
        List<MatchDataUtil.LiveMatch> live;
        try {
            live = MatchDataUtil.getLiveMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchDataUtil.LiveMatch match:live){
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

    // This will prevent in-session repetative awarding.
    private static List<String> cachedAwards = new ArrayList<>();

    public static void checkForPastGames(Guild guild) {
        List<MatchDataUtil.PastMatch> past;
        try {
            past = MatchDataUtil.getPastMatches(true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (MatchDataUtil.PastMatch match : past) {
            ServerDataUtil.ServerData data = ServerDataUtil.getOrCreateServer(guild.getId());
            String pollId = data.prediction_ids.get(match.getMatchId());

            if (pollId == null) continue;

            TextChannel channel = guild.getTextChannelById(data.prediction_channel);
            if (channel == null) continue;

            try {
                Message message = channel.retrieveMessageById(pollId).complete();
                MessagePoll poll = message.getPoll();

                if (poll != null) {
                    if (!cachedAwards.contains(match.getMatchId())){
                        PollAwardUtil.awardFromPoll(
                                guild,
                                message,
                                getWinnerTeam(match)
                        );
                        cachedAwards.add(match.getMatchId());
                    }

                    System.out.println(getWinnerFlag(match));
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(getWinnerTeam(match)+" wins!");
                    embedBuilder.addField("Series details:", match.team1 + " (" + match.score1 + "-" + match.score2 + ") " + match.team2+
                            "\n["+match.tournament_name+"](https://vlr.gg"+match.match_page+")", false);
                    if (getWinnerFlag(match)!=null) {
                        embedBuilder.setThumbnail(getWinnerFlag(match));
                    }
                    if (Math.random()>0.5) {
                        embedBuilder.setFooter("Check /leaderboard to see your points.");
                    } else {
                        embedBuilder.setFooter(StandardMessages.randomRegularAdvert());
                    }
                    message.replyEmbeds(embedBuilder.build()).queue();
                }

                ServerDataUtil.removePrediction(guild.getId(), match.getMatchId());
                ServerDataUtil.save();

                if (poll!=null && !poll.isExpired()) {
                    channel.endPollById(pollId).queue();
                }

            } catch (Exception e) {
                System.err.println("Failed to process match " + match.getMatchId() + ": " + e.getMessage());
            }
        }
    }
}
