package dev.javadrinker.vcpm;

import dev.javadrinker.vcpm.commands.*;
import dev.javadrinker.vcpm.commands.text.ArtificialAwardCommand;
import dev.javadrinker.vcpm.commands.text.TeamByNameCommand;
import dev.javadrinker.vcpm.commands.text.TextCommandManager;
import dev.javadrinker.vcpm.util.Scheduler;
import dev.javadrinker.vcpm.util.VLRTeamUtil;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final Dotenv config = Dotenv.configure().ignoreIfMissing().load();
    public static Dotenv getConfig = config;

    private static final String token = config.get("TOKEN");
    private static JDA jda;

    public static void main(String[] args) throws Exception {

        JDABuilder builder = JDABuilder.createDefault(token);
        jda = builder.setActivity(Activity.watching("VCT"))
                .addEventListeners(
                        new CheckUpcoming(),
                        new LeaderboardCommand(),
                        new SetPredictionChannelCommand(),
                        new FeatureRequestCommand(),
                        new StatsCommand()
                )
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.SCHEDULED_EVENTS
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build()
                .awaitReady();


        upsertCommands();

        TextCommandManager textCommandManager = new TextCommandManager("--");

        textCommandManager.register(new ArtificialAwardCommand());
        textCommandManager.register(new TeamByNameCommand());

        jda.addEventListener(textCommandManager);


        Scheduler.start(jda);
        VLRTeamUtil.loadAllTeams();
    }

    public static JDA getJDA() {
        return jda;
    }

    public static String getVersion() {
        return getConfig.get("VERSION");
    }

    public static void upsertCommands() {
        jda.getGuilds();
        for (Guild guild : jda.getGuilds()) {
            if (guild == null) {
                continue;
            }
            guild.retrieveCommands().queue(existingCommands -> {
                System.out.println("Clearing existing commands for guild [" + guild.getId() + "].");
                AtomicInteger waitlist = new AtomicInteger(existingCommands.size());
                for (var command : existingCommands) {
                    guild.deleteCommandById(command.getId()).queue(cmd -> {
                        System.out.println("Deleted command [" + command.getName() + "] for guild [" + guild.getId() + "].");
                        waitlist.addAndGet(-1);
                    });
                }

                while (waitlist.get() > 0) {
                    // Waiting for deletions to finish
                };

                System.out.println("Loading upserting commands for guild [" + guild.getId() + "].");

                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("upcoming", "Replies with upcoming t1 matches"))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("leaderboard", "leaderboard of top predictors"))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("set-prediction-channel", "THIS WILL CLEAR ALL PREDICTIONS!"))
                        .addOption(OptionType.CHANNEL, "channel", "The channel to set predictions to", true)
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("feature-request", "Request a feature."))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("stats", "See your stats."))
                        .addOption(OptionType.USER, "user", "the user to get stats of", false)
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
            });
        }
    }
}