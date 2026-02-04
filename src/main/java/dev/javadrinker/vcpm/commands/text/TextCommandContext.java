package dev.javadrinker.vcpm.commands.text;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

public class TextCommandContext {

    public final JDA jda;
    public final Message message;
    public final User author;
    public final Member member;
    public final Guild guild;
    public final MessageChannel channel;
    public final List<String> args;
    public final String raw;

    public TextCommandContext(
            JDA jda,
            Message message,
            List<String> args,
            String raw
    ) {
        this.jda = jda;
        this.message = message;
        this.author = message.getAuthor();
        this.member = message.getMember();
        this.guild = message.getGuild();
        this.channel = message.getChannel();
        this.args = args;
        this.raw = raw;
    }

    public String arg(int index) {
        return index < args.size() ? args.get(index) : null;
    }

    public void reply(String content) {
        channel.sendMessage(content).queue();
    }
}
