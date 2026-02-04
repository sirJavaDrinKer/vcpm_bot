package dev.javadrinker.vcpm.commands.text;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public abstract class TextCommand {

    public abstract String getName();

    public abstract void execute(TextCommandContext ctx) throws IOException, InterruptedException;


    public List<String> getAliases() {
        return List.of();
    }

    public String getDescription() {
        return "No description provided.";
    }

    public String getUsage() {
        return getName();
    }

    public Set<Permission> requiredPermissions() {
        return Set.of();
    }

    public boolean guildOnly() {
        return true;
    }

    public long cooldownMillis() {
        return 0;
    }

    /* ===== Helpers ===== */

    public boolean hasPermission(Member member) {
        if (requiredPermissions().isEmpty()) return true;
        return member != null && member.hasPermission(requiredPermissions());
    }
}
