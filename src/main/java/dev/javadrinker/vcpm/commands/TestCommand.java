package dev.javadrinker.vcpm.commands;

import dev.javadrinker.vcpm.util.MatchUtil;
import dev.javadrinker.vcpm.util.ServerDataUtil;
import dev.javadrinker.vcpm.util.UnixConversion;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.messages.MessagePoll;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.List;

public class TestCommand extends ListenerAdapter implements EventListener {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("test")) {
            return;
        }

        event.deferReply().queue();
        StringBuilder response = new StringBuilder();

        event.getGuild().getTextChannelById(1466780706163261717L)
                .retrieveMessageById(event.getOption("input").getAsString())
                .queue(message -> {
                    boolean ready = false;
                    for (MessagePoll.Answer answer : message.getPoll().getAnswers()) {
                        message.retrievePollVoters(answer.getId()).queue(voters -> {
                            response.append("**"+answer.getText()+"**\n");
                            for (User voter : voters) {
                                response.append(voter.getName()).append("\n");
                            }
                            event.getHook().editOriginal(response.toString()).queue();

                        });
                    }
                    ready = true;
                });

        event.getHook().sendMessage("Waiting...").queue();
    }
}
