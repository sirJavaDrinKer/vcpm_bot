package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.Main;
import dev.javadrinker.vcpm.util.StandardMessages;
import dev.javadrinker.vcpm.util.UnixConversion;
import dev.javadrinker.vcpm.util.data.MatchDataUtil;
import dev.javadrinker.vcpm.util.vlr.AbbreviationConverter;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static dev.javadrinker.vcpm.Main.getVersion;

public class BotInformationCommand extends ListenerAdapter implements EventListener {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("bot-information")) {
            return;
        }
        if(!event.isFromGuild()) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        if(event.getGuild() == null) {
            event.reply("Error retrieving server information.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Bot Information");
        embedBuilder.setDescription(
                "Started: <t:"+ Main.startTime()/1000+":R>\n");
        embedBuilder.setFooter("@javadrinker · "+ StandardMessages.randomRegularAdvert());
        embedBuilder.addField("Commands",
            "`/upcoming`\n" +
            "`/toggle-feature`\n" +
            "`/leaderboard`\n" +
            "`/stats`\n" +
            "`/set-channel`\n" +
            "`/feature-request`\n" +
            "`/bot-information`",
        true);

        embedBuilder.addField("Functions",
            "`:` Get upcoming matches (within 24h)\n" +
            "`:` Toggle a certain feature.\n" +
            "`:` View the top predictors.\n" +
            "`:` View your or other's stats.\n" +
            "`:` Set predict/remind/news channel.\n" +
            "`:` Request features, report bugs.\n" +
            "`:` Look at bot info.",
        true);

        embedBuilder.addField("Version & Notes", "v2.1.0 Notable additions are as follows:\n" +
                "- Changed `/do-predictions` to `/toggle-features`, now allowing toggling predictions, news, and reminders.\n" +
                "- Added `news` option to `/set-channel`." +
                "- Added news feature that scans VLR articles and posts summaries to specified channels." +
                "- Removed backend code for multiple unused commands that will not be implemented.", false);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

    }
}
