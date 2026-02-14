package dev.javadrinker.vcpm.commands.text.dev;

import dev.javadrinker.vcpm.commands.text.TextCommand;
import dev.javadrinker.vcpm.commands.text.TextCommandContext;
import dev.javadrinker.vcpm.util.polls.PollAwardUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.messages.MessagePoll;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ArtificialAwardCommand extends TextCommand {

    @Override
    public String getName() {
        return "artificialaward";
    }

    @Override
    public Set<Permission> requiredPermissions() {
        return Set.of(Permission.MANAGE_SERVER);
    }

    @Override
    public String getUsage() {
        return "--artificialAward <poll answer>";
    }

    @Override
    public void execute(TextCommandContext ctx) {
        if (!DevCommands.getDevMode(ctx.guild)) {
            return;
        }

        Message commandMessage = ctx.message;

        Message referenced = commandMessage.getReferencedMessage();
        if (referenced == null) {
            tempReply(ctx, "You must reply to a poll message.");
            return;
        }

        MessagePoll poll = referenced.getPoll();
        if (poll == null) {
            tempReply(ctx, "The replied message is not a poll.");
            return;
        }

        String answerText = String.join(" ", ctx.args).trim();
        if (answerText.isEmpty()) {
            tempReply(ctx, "Usage: `" + getUsage() + "`");
            return;
        }

        boolean valid = poll.getAnswers()
                .stream()
                .anyMatch(a -> a.getText().equalsIgnoreCase(answerText));

        if (!valid) {
            tempReply(ctx, "That answer does not exist in the poll.");
            return;
        }

        PollAwardUtil.awardFromPoll(
                ctx.guild,
                referenced,
                answerText
        );

        commandMessage.addReaction(Emoji.fromFormatted("✅")).queue();
        commandMessage.delete().queueAfter(5, TimeUnit.SECONDS);
    }

    /* ===== Helper ===== */

    private void tempReply(TextCommandContext ctx, String content) {
        ctx.channel.sendMessage(content)
                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
