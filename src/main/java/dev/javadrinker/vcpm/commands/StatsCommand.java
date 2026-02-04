package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.ServerDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class StatsCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getName().equals("stats")) return;

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
            event.reply("No data... yet.")
                    .setEphemeral(true)
                    .queue();
            return;
        }


        String userId;
        User user;
        if (event.getOption("user") != null) {
            userId = event.getOption("user").getAsUser().getId();
            user = guild.getMemberById(userId).getUser();
        } else {
            userId = event.getUser().getId();
            user = event.getUser();
        }

        System.out.println(user.getAsMention());


        ServerDataUtil.UserData userData = data.user_data.get(userId);
        int correct = userData.score;
        int total = userData.total_predictions_made;
        int incorrect = total - correct;

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Your Prediction Stats")
                .setDescription("Here are "+ user.getAsMention()+"'s prediction statistics:")
                .addField("Total Predictions", String.valueOf(total), true)
                .addField("Correct vs. Incorrect", correct+" - "+incorrect, true)
                .addField("Accuracy", user.getAsMention()+" has been right "+Math.round(((float) correct /total)*100)+"% of the time.", false)
                .setThumbnail(event.getUser().getAvatarUrl());

        event.replyEmbeds(embed.build()).setEphemeral(true).queue();
    }
}
