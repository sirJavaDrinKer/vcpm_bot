package dev.javadrinker.vcpm.commands.text;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TextCommandManager extends ListenerAdapter {

    private final String prefix;
    private final Map<String, TextCommand> commandMap = new HashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public TextCommandManager(String prefix) {
        this.prefix = prefix;
    }

    public void register(TextCommand command) {
        commandMap.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commandMap.put(alias.toLowerCase(), command);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String raw = event.getMessage().getContentRaw();
        if (!raw.startsWith(prefix)) return;

        String[] split = raw.substring(prefix.length()).trim().split("\\s+");
        if (split.length == 0) return;

        String invoke = split[0].toLowerCase();
        TextCommand command = commandMap.get(invoke);
        if (command == null) return;

        if (command.guildOnly() && !event.isFromGuild()) return;

        long now = System.currentTimeMillis();
        String cooldownKey = event.getAuthor().getId() + ":" + command.getName();

        if (command.cooldownMillis() > 0) {
            long last = cooldowns.getOrDefault(cooldownKey, 0L);
            if (now - last < command.cooldownMillis()) return;
            cooldowns.put(cooldownKey, now);
        }

        if (!command.hasPermission(event.getMember())) {
            event.getChannel().sendMessage("You don't have permission.").queue();
            return;
        }

        List<String> args = split.length > 1
                ? Arrays.asList(split).subList(1, split.length)
                : List.of();

        try {
            command.execute(new TextCommandContext(
                    event.getJDA(),
                    event.getMessage(),
                    args,
                    raw
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
