package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Set;

public class ToggleFeatureCommand extends ListenerAdapter implements EventListener {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("toggle-features")) {
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You don't have permission to use this command.").queue();
            return;
        }



        boolean enabled = event.getOption("enabled").getAsBoolean();
        String featureName = event.getOption("feature").getAsString();

        switch (featureName) {
            case "prediction":
                ServerDataUtil.setPredictionsEnabled(event.getGuild().getId(), enabled);
                break;
            case "news":
                ServerDataUtil.setNewsEnabled(event.getGuild().getId(), enabled);
                break;
            case "reminders":
                ServerDataUtil.setRemindersEnabled(event.getGuild().getId(), enabled);
                break;
            default:
                event.reply("Unknown feature: `" + featureName+"`. expected `prediction`, `news`, or `reminders`.").setEphemeral(true).queue();
                return;
        }

        event.reply("["+featureName+"] has been " + (enabled ? "enabled." : "disabled.")).queue();
    }
}
