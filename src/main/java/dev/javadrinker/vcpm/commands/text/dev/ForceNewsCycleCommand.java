package dev.javadrinker.vcpm.commands.text.dev;

import dev.javadrinker.vcpm.commands.text.TextCommand;
import dev.javadrinker.vcpm.commands.text.TextCommandContext;
import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ForceNewsCycleCommand extends TextCommand {

    @Override
    public String getName() {
        return "forceNewsCycle";
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
        return "--forceNewsCycle";
    }

    @Override
    public void execute(TextCommandContext ctx) {
        if(!getDevMode(ctx.guild)) {
            tempReply(ctx, "Developer mode is not enabled on this server.");
            return;
        }

        Message commandMessage = ctx.message;
        commandMessage.reply("Forcing news cycle.").queue();
    }

    private void tempReply(TextCommandContext ctx, String content) {
        ctx.channel.sendMessage(content)
                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
