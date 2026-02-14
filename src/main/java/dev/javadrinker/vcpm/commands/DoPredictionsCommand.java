package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DoPredictionsCommand extends ListenerAdapter implements EventListener {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("do-predictions")) {
            return;
        }

        boolean enabled = event.getOption("enabled").getAsBoolean();
        ServerDataUtil.setPredictionsEnabled(event.getGuild().getId(), enabled);

        event.reply("Predictions have been " + (enabled ? "enabled." : "disabled.")).queue();
    }
}
