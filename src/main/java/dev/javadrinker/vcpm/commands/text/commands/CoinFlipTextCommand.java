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

public class CoinFlipTextCommand extends TextCommand {

    @Override
    public String getName() {
        return "coinflip";
    }

    @Override
    public String getUsage() {
        return "--coinflip";
    }

    @Override
    public void execute(TextCommandContext ctx) {
        Message commandMessage = ctx.message;

        commandMessage.reply("Flipping a coin...").queue(reply -> {
            String result = Math.random() < 0.5 ? "Flipping a coin... Heads!" : "Flipping a coin... Tails!";
            reply.editMessage(result).queueAfter(2500, TimeUnit.MILLISECONDS);
        });
    }

    /* ===== Helper ===== */

    private void tempReply(TextCommandContext ctx, String content) {
        ctx.channel.sendMessage(content)
                .queue(msg -> msg.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
