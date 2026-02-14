package dev.javadrinker.vcpm.commands.text.dev;

import dev.javadrinker.vcpm.commands.text.TextCommand;
import dev.javadrinker.vcpm.commands.text.TextCommandContext;
import dev.javadrinker.vcpm.util.data.TeamDataUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TeamByNameCommand extends TextCommand {

    @Override
    public String getName() {
        return "teambyname";
    }

    @Override
    public Set<Permission> requiredPermissions() {
        return Set.of(Permission.MANAGE_SERVER);
    }

    @Override
    public String getUsage() {
        return "--teambyname <name>";
    }

    @Override
    public void execute(TextCommandContext ctx) throws IOException, InterruptedException {
        if (!DevCommands.getDevMode(ctx.guild)) {
            return;
        }

        Message commandMessage = ctx.message;

        String answerText = String.join(" ", ctx.args).trim();
        if (answerText.isEmpty()) {
            tempReply(ctx, "Usage: `" + getUsage() + "`");
            return;
        }

        TeamDataUtil.TeamSummary team = TeamDataUtil.getTeamByName(answerText);
        boolean valid = team != null;

        if (!valid) {
            tempReply(ctx, "Returned null.");
            return;
        }

        commandMessage.addReaction(Emoji.fromFormatted("✅")).queue();
        tempReply(ctx, team.id);
        commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
    }

    /* ===== Helper ===== */

    private void tempReply(TextCommandContext ctx, String content) {
        ctx.channel.sendMessage(content)
                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
