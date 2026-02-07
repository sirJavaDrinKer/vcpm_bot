package dev.javadrinker.vcpm.util.polls;

import dev.javadrinker.vcpm.util.data.ServerDataUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.messages.MessagePoll;

import java.util.HashSet;
import java.util.Set;

public final class PollAwardUtil {

    private PollAwardUtil() {}

    /**
     * Awards points and increments prediction counts from a poll.
     *
     * @param guild         Guild the poll belongs to
     * @param message       Poll message
     * @param winningAnswer The EXACT answer text that won
     */
    public static void awardFromPoll(
            Guild guild,
            Message message,
            String winningAnswer
    ) {

        MessagePoll poll = message.getPoll();
        if (poll == null) return;

        Set<String> processedUsers = new HashSet<>();

        for (MessagePoll.Answer answer : poll.getAnswers()) {

            boolean isWinningAnswer =
                    answer.getText().equalsIgnoreCase(winningAnswer);

            message.retrievePollVoters(answer.getId()).queue(voters -> {
                for (User voter : voters) {

                    // Prevent double processing
                    if (!processedUsers.add(voter.getId())) {
                        continue;
                    }

                    // Increment predictions made
                    ServerDataUtil.incrementPredictionsMade(
                            guild.getId(),
                            voter.getId()
                    );

                    // Award score ONLY if correct
                    if (isWinningAnswer) {
                        ServerDataUtil.addUserScore(
                                guild.getId(),
                                voter.getId(),
                                1
                        );
                    }
                }

                ServerDataUtil.save();
            });
        }
    }
}
