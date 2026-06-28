package dev.javadrinker.vcpm;

import dev.javadrinker.vcpm.commands.*;
import dev.javadrinker.vcpm.commands.text.dev.*;
import dev.javadrinker.vcpm.commands.text.TextCommandManager;
import dev.javadrinker.vcpm.commands.text.dev.CoinFlipTextCommand;
import dev.javadrinker.vcpm.util.Scheduler;
import dev.javadrinker.vcpm.util.data.TeamDataUtil;
import dev.javadrinker.vcpm.util.polls.NewsCycle;
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

    public static final String VLRAPI_SELF = config.get("VLRAPI_SELF");

    private static long startTime;

    public static void main(String[] args) throws Exception {

        JDABuilder builder = JDABuilder.createDefault(token);
        jda = builder.setActivity(Activity.watching("VCT"))
                .addEventListeners(
                        new CheckUpcoming(),
                        new LeaderboardCommand(),
                        new SetChannelCommand(),
                        new FeatureRequestCommand(),
                        new StatsCommand(),
                        new ToggleFeatureCommand(),
                        new BotInformationCommand(),
                        new NewsCycle(),
                        new ResetLeaderboard()
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

        startTime = System.currentTimeMillis();

        upsertCommands();

        TextCommandManager textCommandManager = new TextCommandManager("--");

        textCommandManager.register(new ArtificialAwardCommand());
        textCommandManager.register(new TeamByNameCommand());
        textCommandManager.register(new DevCommands());
        textCommandManager.register(new ForceNewsCycleCommand());
        textCommandManager.register(new CoinFlipTextCommand());

        jda.addEventListener(textCommandManager);


        TeamDataUtil.loadAllTeamsAsync();
        Scheduler.start(jda);
    }

    public static JDA getJDA() {
        return jda;
    }

    public static String getVersion() {
        return getConfig.get("v"+Main.class.getPackage().getImplementationVersion());
    }
    public static long startTime() {
        return startTime;
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
                    /*guild.deleteCommandById(command.getId()).queue(cmd -> {
                        System.out.println("Deleted command [" + command.getName() + "] for guild [" + guild.getId() + "].");
                        waitlist.addAndGet(-1);
                    });*/
                }

                while (waitlist.get() > 0) {
                    // Waiting for deletions to finish
                };

                System.out.println("Loading upserting commands for guild [" + guild.getId() + "].");

                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("upcoming", "Replies with upcoming t1 matches"))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("reset-leaderboard", "Resets the leaderboard."))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("leaderboard", "leaderboard of top predictors"))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("set-channel", "Set where certain bot messages are sent."))
                        .addOption(OptionType.STRING, "type", "The type of channel to set. 'news', 'reminder' or 'prediction'.", true)
                        .addOption(OptionType.CHANNEL, "channel", "The channel to set predictions to", true)
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("feature-request", "Request a feature."))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("stats", "See your stats."))
                        .addOption(OptionType.USER, "user", "the user to get stats of", false)
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("toggle-features", "Enable/disable predictions."))
                        .addOption(OptionType.STRING, "feature", "the feature to enable or disable. 'news', 'reminder' or 'prediction'.", true)
                        .addOption(OptionType.BOOLEAN, "enabled", "either disable or enable predictions for this server", true)
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
                Objects.requireNonNull(jda.getGuildById(guild.getId()).upsertCommand("bot-information", "Get information about the bot."))
                        .queueAfter(1, java.util.concurrent.TimeUnit.SECONDS);
            });
        }
    }
}