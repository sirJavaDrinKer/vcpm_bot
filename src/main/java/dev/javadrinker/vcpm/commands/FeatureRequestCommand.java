package dev.javadrinker.vcpm.commands;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.modals.Modal;

import java.awt.*;

public class FeatureRequestCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getName().equals("feature-request")) return;

        TextInput title = TextInput.create("title", TextInputStyle.SHORT)
                .setPlaceholder("Short summary")
                .setRequired(true)
                .setMaxLength(100)
                .build();

        TextInput description = TextInput.create("description",  TextInputStyle.PARAGRAPH)
                .setPlaceholder("Explain what you'd like added or changed")
                .setRequired(true)
                .setMaxLength(1000)
                .build();

        Modal modal = Modal.create("feature_request_modal", "Submit a Feature Request")
                .addComponents(
                        Label.of("Title", title),
                        Label.of("Description", description)
                )
                .build();

        event.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!event.getModalId().equals("feature_request_modal")) return;

        String title = event.getValue("title").getAsString();
        String description = event.getValue("description").getAsString();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Feature Request")
                .setColor(Color.GREEN)
                .addField(title, description, false)
                .addField("From:",
                        event.getUser().getAsMention(),
                        true);

        event.reply("Feature request submitted!").setEphemeral(true).queue();

        event.getJDA().retrieveUserById(Dotenv.load().get("OWNER_ID")).queue(owner ->
                owner.openPrivateChannel().queue(channel ->
                        channel.sendMessageEmbeds(embed.build()).queue()
                )
        );
    }
}
