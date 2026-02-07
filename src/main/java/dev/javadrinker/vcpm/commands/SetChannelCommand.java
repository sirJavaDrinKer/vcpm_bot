package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.attribute.IPermissionContainer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

public class SetChannelCommand extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("set-channel")) {
            return;
        }
        if (!event.getMember().hasPermission((IPermissionContainer) event.getGuildChannel(), Permission.MANAGE_CHANNEL)) {
            event.reply("You do not have permission to set the prediction channel.").setEphemeral(true).queue();
            return;
        }

        String type = event.getOption("type").getAsString();
        Channel predictionChannel = event.getOption("channel").getAsChannel();

        ServerDataUtil.setPredictionChannel(
                event.getGuild().getId(),
                predictionChannel.getId()
        );
        ServerDataUtil.ServerData serverDataUtil = ServerDataUtil.getOrCreateServer(event.getGuild().getId());

        event.reply(
                "Prediction channel has been set to "
                +predictionChannel.getAsMention()+". All predictions have been cleared."
        ).queue();
    }
}
