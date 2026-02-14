package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SetChannelCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getName().equals("set-channel")) return;

        if (event.getGuild() == null || event.getMember() == null) {
            event.reply("This command can only be used in a server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You do not have permission to configure bot channels.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String type = event.getOption("type").getAsString().toLowerCase();
        GuildChannel channel = event.getOption("channel").getAsChannel();

        String guildId = event.getGuild().getId();
        ServerDataUtil.ServerData serverData =
                ServerDataUtil.getOrCreateServer(guildId);

        switch (type) {

            case "prediction" -> {
                String oldChannel = serverData.prediction_channel;

                ServerDataUtil.setPredictionChannel(guildId, channel.getId());

                if (oldChannel == null || !oldChannel.equals(channel.getId())) {
                    serverData.prediction_ids.clear();
                    ServerDataUtil.save();
                }

                event.reply(
                        "Prediction channel set to: " + channel.getAsMention() +
                                "\n(All existing predictions have been cleared.)"
                ).queue();
            }

            case "reminder" -> {
                ServerDataUtil.setReminderChannel(guildId, channel.getId());

                event.reply(
                        "Reminder channel set to: " + channel.getAsMention()
                ).queue();
            }

            default -> {
                event.reply("Invalid type. Use `prediction` or `reminder`.")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
