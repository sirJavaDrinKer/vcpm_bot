package dev.javadrinker.vcpm.commands.text.dev;

import dev.javadrinker.vcpm.commands.text.TextCommand;
import dev.javadrinker.vcpm.commands.text.TextCommandContext;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DevCommands extends TextCommand {

    @Override
    public String getName() {
        return "toggleDevCommands";
    }

    @Override
    public Set<Permission> requiredPermissions() {
        return Set.of(Permission.MANAGE_SERVER);
    }

    public static Boolean getDevMode(Guild guild) {
        return ServerDataUtil.isDeveloperMode(guild.getId());
    }

    @Override
    public String getUsage() {
        return "--toggleDevMode";
    }

    @Override
    public void execute(TextCommandContext ctx) {

        Message commandMessage = ctx.message;
        System.out.println("Dev mode");

        ServerDataUtil.toggleDeveloperMode(commandMessage.getGuild().getId());
        commandMessage.reply("Developer mode is now "+ (getDevMode(commandMessage.getGuild()) ? "enabled" : "disabled")+".").queue();
        ServerDataUtil.save();
    }

    private void tempReply(TextCommandContext ctx, String content) {
        ctx.channel.sendMessage(content)
                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
