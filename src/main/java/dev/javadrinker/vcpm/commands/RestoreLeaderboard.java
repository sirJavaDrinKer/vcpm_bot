package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RestoreLeaderboard extends ListenerAdapter implements EventListener {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("restore-leaderboard")) {
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER) && !event.getMember().isOwner()) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        if (event.getOption("code") == null) {
            event.reply("You must provide a restore code.").setEphemeral(true).queue();
            return;
        }

        String restoreCode = event.getOption("code").getAsString();
        boolean success = ServerDataUtil.restoreFromCode(event.getGuild().getId(), restoreCode);

        if (success) {
            event.reply("Leaderboard has been successfully restored.").setEphemeral(true).queue();
        } else {
            event.reply("This restore code appears to be corrupted.").setEphemeral(true).queue();
        }
    }
}
