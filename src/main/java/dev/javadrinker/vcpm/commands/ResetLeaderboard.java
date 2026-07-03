package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class ResetLeaderboard extends ListenerAdapter implements EventListener {


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("reset-leaderboard")) {
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !event.getMember().isOwner()) {
            event.reply("You don't have permission to use this command.").queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Reset Leaderboard");
        embedBuilder.setDescription("Are you sure you want to reset the leaderboard?");
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true)
                .addComponents(
                        ActionRow.of(
                                Button.danger("confirm_reset", "Confirm Reset")
                        )
                ).queue();

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().equals("confirm_reset")) {
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !event.getMember().isOwner()) {
            event.reply("You don't have permission to reset the leaderboard.").setEphemeral(true).queue();
            return;
        }

        // Reset the leaderboard data here
        String restoreCode = ServerDataUtil.zeroOutAllUserData(event.getGuild().getId());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Leaderboard Reset");
        embedBuilder.setDescription("The leaderboard has been successfully reset.");
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

        event.getUser().openPrivateChannel().queue((channel) -> {
            channel.sendMessage("Your restore code is:\n\n `"+restoreCode+"`").queue();
        });
    }

}
