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
            "`/do-predictions`\n" +
            "`/leaderboard`\n" +
            "`/stats`\n" +
            "`/set-channel`\n" +
            "`/feature-request`\n" +
            "`bot-information`",
        true);

        embedBuilder.addField("Functions",
            "`:` Get upcoming matches (within 24h)\n" +
            "`:` Toggle predictions.\n" +
            "`:` View the top predictors.\n" +
            "`:` View your or other's stats.\n" +
            "`:` Set predict/remind channel.\n" +
            "`:` Request features, report bugs.\n" +
            "`:` Look at bot info.",
        true);

        embedBuilder.addField("Version & Notes", "v2.0.0 Notable additions are as follows:\n" +
                "- Reorganized packages and utilities to clarify responsibility.\n" +
                "- Added `cached_teams.json`, allowing team information to be accessed while starting API calls are being made.\n" +
                "- Poll reminder and match reminder behavior now implemented, match reminders can now be set to be sent in a specified channel.\n" +
                "- Leaderboard command now supports multiple pages of users." +
                "- Changed scheduler (heartbeat) to 2 minutes to prevent API stress and to reduce chances of timeouts." +
                "- Began caching upcoming matches to allow them to always be accessed.", false);

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

    }
}
