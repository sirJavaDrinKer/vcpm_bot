package dev.javadrinker.vcpm.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class ResetLeaderboard extends ListenerAdapter implements EventListener {
    private static HashMap<String, ArrayList<Integer>> correctOrder = new HashMap<>();
    private static HashMap<String, ArrayList<Integer>> inputOrder = new HashMap<>();


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("reset-leaderboard")) {
            return;
        }

        if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
            event.reply("You don't have permission to use this command.").queue();
            return;
        }

        ArrayList<Integer> randomConfirmation = new ArrayList<Integer>();
        for (int i = 0; i < 3; i++) {
            while (true) {
                int randomNum = new Random().nextInt(10,100);
                if (!randomConfirmation.contains(randomNum)) {
                    randomConfirmation.add(randomNum);
                    break;
                }
            }
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Reset Leaderboard");

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Are you sure you want to reset the leaderboard? Please press: `")
                .append(randomConfirmation.get(0))
                .append("`, `")
                .append(randomConfirmation.get(1))
                .append("`, `")
                .append(randomConfirmation.get(2))
                .append("` in that order to confirm. This action cannot be undone.");
        embedBuilder.setDescription(stringBuilder.toString());

        String userId = event.getUser().getId();

        ArrayList<Integer> used = new ArrayList<Integer>();
        Button[] buttons = new Button[3];
        for (int i = 0; i < 3; i++) {
            int num;
            while(true) {
                num = randomConfirmation.get(new Random().nextInt(3));
                if (!used.contains(num)) {
                    used.add(num);
                    break;
                }
            }
            buttons[i] = Button.danger("confirm-reset_"+i+"_"+num+"_"+userId, num+"");
        }

        event.replyEmbeds(embedBuilder.build())
                .addComponents(ActionRow.of(buttons[0], buttons[1], buttons[2], Button.secondary("confirm-reset_cancel_"+userId, "Cancel")))
                .queue(msg -> {
                    correctOrder.put(msg.getId(), randomConfirmation);
                    System.out.println(correctOrder.get(msg.getId()));
                    System.out.println(msg.getId());
                });
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("confirm-reset_cancel_")) {
            String[] parts = event.getComponentId().split("_");
            if (!parts[2].equals(event.getUser().getId())) {
                event.reply("You may not interact with this.").setEphemeral(true).queue();
                return;
            }
            event.reply("Leaderboard reset cancelled.").setEphemeral(true).queue();
            cleanUp(event.getMessageId());

            event.getMessage().delete().queue();
            cleanUp(event.getMessageId());
            return;
        }

        if (!event.getComponentId().startsWith("confirm-reset_")) {
            return;
        }

        String[] parts = event.getComponentId().split("_");

        if (!parts[3].equals(event.getUser().getId())) {
            event.reply("You may not interact with this.").setEphemeral(true).queue();
            return;
        }

        int parsed = Integer.parseInt(parts[2]);

        if (!inputOrder.containsKey(event.getMessageId())) {
            inputOrder.put(event.getMessageId(), new ArrayList<>());
        }

        inputOrder.get(event.getMessageId()).add(parsed);

        if (inputOrder.get(event.getMessageId()).size() == 3) {
            if (inputOrder.get(event.getMessageId()).equals(correctOrder.get(event.getMessageId()))) {
                event.reply("Leaderboard has been reset.").queue();
            } else {
                System.out.println(correctOrder.get(event.getMessageId())+" vs "+inputOrder.get(event.getMessageId()));
                System.out.println(event.getMessageId());
                event.reply("Incorrect confirmation order. Leaderboard reset cancelled.").setEphemeral(true).queue();
                event.getMessage().delete().queue();
            }
            inputOrder.remove(event.getMessageId());
        } else {
            event.editButton(event.getButton().asDisabled()).queue();
        }

    }

    private void cleanUp(String msgId) {
        correctOrder.remove(msgId);
        inputOrder.remove(msgId);
    }

}
